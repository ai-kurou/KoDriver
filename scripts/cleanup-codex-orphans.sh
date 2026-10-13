#!/usr/bin/env bash
# 孤児化したCodex brokerとその子孫を、安全確認のうえ終了するスクリプト。
# Codex委任・CI待機の実行中には使用しない。--dry-run は対象の表示のみ行う。
set -euo pipefail

DRY_RUN=false
WORKTREE=""
while [ "$#" -gt 0 ]; do
  case "$1" in
    --dry-run) DRY_RUN=true; shift ;;
    --worktree)
      if [ "$#" -lt 2 ] || [[ "$2" != /* ]]; then
        echo '--worktree にはワークツリーの絶対パスを指定してください。' >&2
        exit 1
      fi
      WORKTREE="$2"
      shift 2
      ;;
    *) echo "不明な引数: $1" >&2; exit 1 ;;
  esac
done

# 一覧は一度だけ取得し、同じスナップショットから対象と子孫を決める。
SNAPSHOT="$(ps -axo pid=,ppid=,etime=,command=)"
TARGETS="$(printf '%s\n' "$SNAPSHOT" | CLEANUP_WORKTREE="$WORKTREE" awk '
  {
    pid = $1; parent[pid] = $2; elapsed = $3
    sub(/^[[:space:]]*[0-9]+[[:space:]]+[0-9]+[[:space:]]+[^[:space:]]+[[:space:]]+/, "")
    command[pid] = $0
    order[++count] = pid
    executable = $0
    sub(/[[:space:]].*$/, "", executable)
    cwd = ""
    if (match($0, /(^|[[:space:]])--cwd[[:space:]]+/)) {
      cwd = substr($0, RSTART + RLENGTH)
      sub(/[[:space:]]+--[^[:space:]]+.*$/, "", cwd)
    }
    old = elapsed ~ /-/ || gsub(/:/, ":", elapsed) >= 2
    if (parent[pid] == 1 && executable ~ /(^|\/)node$/ &&
        $0 ~ /openai-codex\/codex\/[^\/[:space:]]+\/scripts\/app-server/ &&
        $0 !~ /CodexBar\.app/ &&
        (old || (ENVIRON["CLEANUP_WORKTREE"] != "" && cwd == ENVIRON["CLEANUP_WORKTREE"]))) {
      root[pid] = 1
    }
  }
  END {
    # 訪問済みPIDをキュー投入時に記録し、各PIDを最大一度だけ処理する。
    for (i = 1; i <= count; i++) {
      pid = order[i]
      if (root[pid] && !visited[pid]) {
        visited[pid] = 1; queue[++tail] = pid
      }
    }
    for (head = 1; head <= tail; head++) {
      pid = queue[head]
      for (i = 1; i <= count; i++) {
        child = order[i]
        if (parent[child] == pid && !visited[child] && command[child] !~ /CodexBar\.app/) {
          visited[child] = 1; queue[++tail] = child
        }
      }
    }
    for (i = 1; i <= tail; i++) {
      pid = queue[i]
      printf "%s\t%d\t%s\n", pid, root[pid] ? 1 : 0, command[pid]
    }
  }
')"

PIDS=()
COMMANDS=()
ROOTS=()
TOTAL=0
ROOT_COUNT=0
while IFS=$'\t' read -r pid root command; do
  [ -n "$pid" ] || continue
  PIDS[$TOTAL]="$pid"
  COMMANDS[$TOTAL]="$command"
  ROOTS[$TOTAL]="$root"
  TOTAL=$((TOTAL + 1))
  ROOT_COUNT=$((ROOT_COUNT + root))
done <<< "$TARGETS"
printf '対象ルート数: %d、対象総数: %d\n' "$ROOT_COUNT" "$TOTAL"
if [ "$DRY_RUN" = true ]; then
  [ -z "$TARGETS" ] || printf '%s\n' "$TARGETS"
  exit 0
fi

same_command() {
  local current
  current="$(ps -o command= -p "${PIDS[$1]}" 2>/dev/null)" || return 1
  [ "$current" = "${COMMANDS[$1]}" ]
}

for ((i = 0; i < TOTAL; i++)); do
  if same_command "$i"; then
    kill -TERM "${PIDS[$i]}" 2>/dev/null || true
  fi
done
if [ "$TOTAL" -gt 0 ]; then
  sleep 3
fi
for ((i = 0; i < TOTAL; i++)); do
  if same_command "$i"; then
    kill -KILL "${PIDS[$i]}" 2>/dev/null || true
  fi
done

# 終了後のスナップショットで、記録したコマンドが残っているかを確認する。
AFTER="$(ps -axo pid=,command=)"
ENDED_ROOTS=0
ENDED_TOTAL=0
for ((i = 0; i < TOTAL; i++)); do
  if ! printf '%s\n' "$AFTER" | CLEANUP_COMMAND="${COMMANDS[$i]}" awk -v pid="${PIDS[$i]}" '
    $1 == pid {
      sub(/^[[:space:]]*[0-9]+[[:space:]]+/, "")
      if ($0 == ENVIRON["CLEANUP_COMMAND"]) found = 1
    }
    END { exit !found }
  '; then
    ENDED_TOTAL=$((ENDED_TOTAL + 1))
    ENDED_ROOTS=$((ENDED_ROOTS + ROOTS[i]))
  fi
done
REMAINING="$(ps -axo command= | awk '/codex/ { count++ } END { print count + 0 }')"
printf '終了ルート数: %d、終了総数: %d、残存 codex プロセス数: %d\n' \
  "$ENDED_ROOTS" "$ENDED_TOTAL" "$REMAINING"
