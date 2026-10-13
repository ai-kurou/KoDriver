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

process_identity() {
  # PID再利用で別プロセスを終了しないよう、開始時刻とコマンドの組で同一性を判定する。
  ps -o lstart=,command= -p "$1" 2>/dev/null
}

PIDS=()
IDENTITIES=()
ROOTS=()
TOTAL=0
ROOT_COUNT=0
while IFS=$'\t' read -r pid root command; do
  [ -n "$pid" ] || continue
  identity="$(process_identity "$pid")" || continue
  # スナップショット後に別コマンドへ置き換わったPIDは対象にしない。
  case "$identity" in *"$command") ;; *) continue ;; esac
  PIDS[TOTAL]="$pid"
  IDENTITIES[TOTAL]="$identity"
  ROOTS[TOTAL]="$root"
  TOTAL=$((TOTAL + 1))
  ROOT_COUNT=$((ROOT_COUNT + root))
done <<< "$TARGETS"
printf '対象ルート数: %d、対象総数: %d\n' "$ROOT_COUNT" "$TOTAL"
if [ "$DRY_RUN" = true ]; then
  [ -z "$TARGETS" ] || printf '%s\n' "$TARGETS"
  exit 0
fi

same_process() {
  local current
  current="$(process_identity "${PIDS[$1]}")" || return 1
  [ "$current" = "${IDENTITIES[$1]}" ]
}

for ((i = 0; i < TOTAL; i++)); do
  if same_process "$i"; then
    kill -TERM "${PIDS[$i]}" 2>/dev/null || true
  fi
done
if [ "$TOTAL" -gt 0 ]; then
  sleep 3
fi
for ((i = 0; i < TOTAL; i++)); do
  if same_process "$i"; then
    kill -KILL "${PIDS[$i]}" 2>/dev/null || true
  fi
done

# KILLは消滅を待たないため、上限付きで終了を待ってから集計する。
for ((attempt = 0; attempt < 10; attempt++)); do
  alive=false
  for ((i = 0; i < TOTAL; i++)); do
    if same_process "$i"; then alive=true; break; fi
  done
  [ "$alive" = true ] || break
  sleep 0.5
done

ENDED_ROOTS=0
ENDED_TOTAL=0
for ((i = 0; i < TOTAL; i++)); do
  if ! same_process "$i"; then
    ENDED_TOTAL=$((ENDED_TOTAL + 1))
    ENDED_ROOTS=$((ENDED_ROOTS + ROOTS[i]))
  fi
done
# このスクリプト自身や集計用awkを数えないよう、パターンは環境変数で渡し自PIDを除外する。
REMAINING="$(ps -axo pid=,command= | \
  CODEX_PATTERN='openai-codex/codex/|/codex app-server|codex-darwin-|/bin/codex( |$)' \
  awk -v self="$$" '$1 != self && $0 ~ ENVIRON["CODEX_PATTERN"] { count++ } END { print count + 0 }')"
printf '終了ルート数: %d、終了総数: %d、残存 codex プロセス数: %d\n' \
  "$ENDED_ROOTS" "$ENDED_TOTAL" "$REMAINING"
