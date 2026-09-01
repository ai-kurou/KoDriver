#!/usr/bin/env bash
# ローカルMac（launchd）から夜間実装バッチを実行するスクリプト。
# .github/workflows/nightly-implement.yml（GitHub Actions版、削除済み）と同じロジックを
# ローカル実行向けに移植したもの。インストール手順は docs/ci-workflows.md を参照。
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${REPO_ROOT}"

REPO="$(gh repo view --json nameWithOwner --jq .nameWithOwner)"
LOG_PREFIX="[nightly-implement-local]"

# log outputs a timestamped message with the script's log prefix.
log() {
  echo "${LOG_PREFIX} $(date '+%Y-%m-%d %H:%M:%S') $*"
}

git fetch origin main

# 検索演算子 `-linked:pr` は過去にマージ済み・クローズ済みのPRが本文で issue 番号に
# 言及しただけでも「linked」と判定してしまうため使わず、GraphQL の
# closingIssuesReferences で「現在オープン中のPRが実際にそのissueをクローズする予定か」を
# 個別に確認する（nightly-implement.yml と同じ理由、#1286, #1290）。
all_issues="$(gh issue list \
  --repo "${REPO}" \
  --search "is:open label:claude-implementable" \
  --json number,title,url \
  --limit 1000 \
  | jq -c 'sort_by(.number)')"

blocked_issue_numbers="$(gh api graphql -f query='
  query($owner: String!, $name: String!) {
    repository(owner: $owner, name: $name) {
      pullRequests(states: OPEN, first: 100) {
        nodes { closingIssuesReferences(first: 20) { nodes { number } } }
      }
    }
  }' -f owner="${REPO%%/*}" -f name="${REPO##*/}" \
  --jq '[.data.repository.pullRequests.nodes[].closingIssuesReferences.nodes[].number] | unique')"

issue_json="$(jq -c --argjson blocked "${blocked_issue_numbers}" \
  '[.[] | select(.number as $n | $blocked | index($n) | not)] | .[0] // empty' \
  <<< "${all_issues}")"

if [ -z "${issue_json}" ]; then
  log "対象issueなし。終了します。"
  exit 0
fi

issue_number="$(jq -r '.number' <<< "${issue_json}")"
issue_title="$(jq -r '.title' <<< "${issue_json}")"
issue_url="$(jq -r '.url' <<< "${issue_json}")"
branch_name="claude/issue-${issue_number}"
worktree_dir="${REPO_ROOT}/.claude/worktrees/issue-${issue_number}"

log "issue #${issue_number} (${issue_title}) を選択しました。"

if git worktree list --porcelain | grep -qx "worktree ${worktree_dir}"; then
  log "ワークツリー ${worktree_dir} は既に存在します。他セッションが使用中の可能性があるためスキップします。"
  exit 0
fi
if git show-ref --verify --quiet "refs/heads/${branch_name}"; then
  log "ブランチ ${branch_name} は既に存在します。スキップします。"
  exit 0
fi

git worktree add "${worktree_dir}" -b "${branch_name}" origin/main

# local.properties はマシン固有でgit管理外のため新規ワークツリーには存在せず、
# そのままだと preSubmitChecks が「SDK location not found」で失敗する。
# ただし local.properties には app/androidApp/build.gradle.kts が読む
# STORE_PASSWORD 等の署名鍵の秘匿情報が含まれる場合があり、ファイルを
# 丸ごと複製すると Claude Code CLI（Bashツールでワークツリー内を読める）に
# 露出してしまう。preSubmitChecks に必要な sdk.dir のみを複製する。
if [ -f "${REPO_ROOT}/local.properties" ]; then
  grep '^sdk\.dir=' "${REPO_ROOT}/local.properties" > "${worktree_dir}/local.properties" || true
fi

# cleanup removes the dedicated worktree and its local branch, ignoring errors when either is already absent.
cleanup() {
  git worktree remove --force "${worktree_dir}" 2>/dev/null || true
  git branch -D "${branch_name}" 2>/dev/null || true
}

pre_comment_count="$(gh issue view "${issue_number}" --repo "${REPO}" --json comments --jq '.comments | length')"

status_file="${worktree_dir}/.nightly-implement-status"
commit_message_file="${worktree_dir}/.nightly-implement-commit-message.txt"
pr_title_file="${worktree_dir}/.nightly-implement-pr-title.txt"
pr_body_file="${worktree_dir}/.nightly-implement-pr-body.txt"
rm -f "${status_file}" "${commit_message_file}" "${pr_title_file}" "${pr_body_file}"

set +e
(cd "${worktree_dir}" && claude -p "$(cat <<PROMPT
KoDriverリポジトリの夜間バッチ処理として、GitHub issue #${issue_number}（${issue_url}）を実装してください。
このステップでは実装（コード変更・テスト追加）とPR用文言の下書きまでを行い、\`./gradlew preSubmitChecks\` の実行・コミット・プッシュ・PR作成は行わない（それらは実装完了後、このスクリプトの後続処理が機械的に行う）。

1. \`gh issue view ${issue_number} --repo ${REPO} --json title,body,comments\` でissueの内容（本文・コメント）を確認する。
2. CLAUDE.md の「実装前」チェックリストに従い、既存実装・依存モジュール・類似コードを確認する。
3. issueの要件が実装するには曖昧・情報不足だと判断した場合は、コード変更は一切行わず、\`gh issue comment ${issue_number} --repo ${REPO}\` で「要件が不十分なため保留する」旨と不足している情報を日本語で具体的にコメントする。その後 \`echo "insufficient" > ${status_file}\` を実行して終了する（issueのラベルは外さない）。
4. 実装可能と判断した場合は実装する（既に \`${branch_name}\` ブランチにチェックアウト済みのため、ブランチ作成は不要）。CLAUDE.md のテスト方針・コーディング規約・Git操作ルールに従うこと。ユニットテストが必要な変更には同時にテストを追加・更新する。\`docs/improvement-ideas.md\` への新規追加・既存項目の書き換えは行わない（承認が必要なファイルのため）。ただし、\`docs/improvement-ideas.md\` に今回実装したissueの内容と一致する項目が既に存在する場合に限り、その項目のみを削除してよい（この場合、issue自体がユーザー承認済みの実装依頼であるため、削除に別途ユーザー承認は不要）。削除する場合は、実装本体と同じPRに含め、CLAUDE.md の規定どおり \`docs/resolved-improvement-ideas.md\` に \`- YYYY-MM-DD 一言サマリ（関連PR #番号）\` 形式で1行追記すること。
   実装が完了したら、以下のファイルを作成する（コミット・プッシュ・PR作成・\`preSubmitChecks\`の実行は絶対に行わないこと。後続処理が機械的に行うため）。
   - \`${commit_message_file}\`: わかりやすい日本語のコミットメッセージ（1行目に要約、必要なら本文）
   - \`${pr_title_file}\`: 日本語のPRタイトル（1行のみ）
   - \`${pr_body_file}\`: 日本語のPR説明本文（署名やセッションURLは含めない。末尾に "Closes #${issue_number}" を含める）
   - 最後に \`echo "implemented" > ${status_file}\` を実行する。
5. 実装を完遂できないと判断した場合（実装中に解決できない問題に遭遇した等）は、変更を破棄し（\`git checkout -- .\` \`git clean -fd\` 等でクリーンな状態に戻す）、\`gh issue comment ${issue_number} --repo ${REPO}\` で理由を日本語で具体的にコメントし、\`echo "failed" > ${status_file}\` を実行して終了する（ラベルを外す処理は後続処理が行う）。
6. 最後に、実施した内容（実装/保留/失敗のいずれか）を簡潔に出力する。
PROMPT
)" \
  --allowedTools "Read,Grep,Glob,Edit,Write,Bash" \
  --permission-mode acceptEdits \
  --model sonnet)
claude_exit=$?
set -e
log "Claude Code CLI が終了しました（exit code: ${claude_exit}）"

status="unknown"
if [ -f "${status_file}" ]; then
  status="$(cat "${status_file}")"
fi
log "status=${status}"

if [ "${status}" = "implemented" ]; then
  if [ ! -s "${commit_message_file}" ] || [ ! -s "${pr_title_file}" ] || [ ! -s "${pr_body_file}" ] \
    || ! grep -q "Closes #${issue_number}" "${pr_body_file}"; then
    log "statusはimplementedですが、PR用文言の下書きが不足しているため無応答中断として扱います。"
    status="unknown"
    echo "unknown" > "${status_file}"
  fi
fi

if [ "${status}" != "implemented" ]; then
  (cd "${worktree_dir}" && git checkout -- . 2>/dev/null || true; git clean -fd 2>/dev/null || true)

  if [ "${status}" = "insufficient" ]; then
    log "issue #${issue_number} は情報不足のため保留されました。"
    cleanup
    exit 0
  fi

  if [ "${status}" = "failed" ]; then
    log "issue #${issue_number} の実装に失敗しました。"
    gh issue edit "${issue_number}" --repo "${REPO}" --remove-label claude-implementable
    cleanup
    exit 0
  fi

  post_comment_count="$(gh issue view "${issue_number}" --repo "${REPO}" --json comments --jq '.comments | length')"
  if [ "${post_comment_count:-0}" != "${pre_comment_count:-0}" ]; then
    log "statusが不明('${status}')ですが、コメントは投稿済みのためラベルはそのままにします。"
    cleanup
    exit 0
  fi

  gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）による自動実装中に応答が完了しないまま処理が終了したため中断しました（Claude Code CLI exit code: ${claude_exit}）。ラベルを外すので、内容を見直したうえで再度ラベルを付与してください。"
  gh issue edit "${issue_number}" --repo "${REPO}" --remove-label claude-implementable
  cleanup
  exit 0
fi

set +e
(cd "${worktree_dir}" && ./gradlew preSubmitChecks)
presubmit_exit=$?
set -e

if [ "${presubmit_exit}" != "0" ]; then
  log "preSubmitChecks が失敗しました（exit code: ${presubmit_exit}）"
  (cd "${worktree_dir}" && git checkout -- . 2>/dev/null || true; git clean -fd 2>/dev/null || true)
  gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）による自動実装後、\`./gradlew preSubmitChecks\` が失敗したため変更を破棄しました（exit code: ${presubmit_exit}）。"
  gh issue edit "${issue_number}" --repo "${REPO}" --remove-label claude-implementable
  cleanup
  exit 0
fi

cd "${worktree_dir}"

# モジュール図・スクリーンショット画像はCIで自動更新される仕組みのため、
# preSubmitChecksの実行中に生成・変更されていてもコミットに含めない。
# このスクリプトが作成した runner-control ファイル（.nightly-implement-*）も、
# gitignore対象ではなく git add -A で拾われてしまうため同様に除外する。
git add -A
# macOS標準の/bin/bashはbash 3.2で mapfile（bash 4.0以降のビルトイン）が使えないため、while read で代替する。
excluded_files=()
while IFS= read -r excluded_file; do
  excluded_files+=("${excluded_file}")
done < <(git diff --cached --name-only | grep -E '(^|/)snapshots/[^/]+\.png$|^docs/graphs/[^/]+\.(gv|svg)$|^\.nightly-implement-' || true)
if [ "${#excluded_files[@]}" -gt 0 ]; then
  git reset -- "${excluded_files[@]}"
fi

if git diff --cached --quiet; then
  log "除外後にコミット対象の変更がありませんでした。"
  gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）による自動実装後、コミット対象の変更が見つかりませんでした。ラベルを外すので、内容を見直したうえで再度ラベルを付与してください。"
  gh issue edit "${issue_number}" --repo "${REPO}" --remove-label claude-implementable
  cd "${REPO_ROOT}"
  cleanup
  exit 0
fi

git commit -F "${commit_message_file}"

# push/PR作成のいずれかが失敗した場合、set -e により即座に終了すると
# ワークツリー・ローカルブランチが残り続け、次回以降の実行が
# 「ブランチ/ワークツリーが既に存在する」としてこのissueをスキップし続けてしまう。
# push成功後はコミットを復旧できるようリモートブランチ・ワークツリーを残し、
# push自体が失敗した場合のみ後始末（cleanup）する。
pushed=false
# publish_failure handles failures during commit publication by notifying the issue and either preserving the pushed branch for manual PR creation or removing the label and cleaning up.
publish_failure() {
  if [ "${pushed}" = true ]; then
    gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）による自動実装後、コミットのプッシュ後にPR作成が失敗しました。ブランチ \`${branch_name}\` にコミット済みのため、手動でPRを作成してください。"
  else
    gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）による自動実装後、コミットのプッシュに失敗したため中断しました。ラベルを外すので、内容を見直したうえで再度ラベルを付与してください。"
    gh issue edit "${issue_number}" --repo "${REPO}" --remove-label claude-implementable
    cd "${REPO_ROOT}"
    cleanup
  fi
}
trap publish_failure ERR

git push -u origin "${branch_name}"
pushed=true

pr_url="$(gh pr create --repo "${REPO}" --base main --head "${branch_name}" --title "$(cat "${pr_title_file}")" --body-file "${pr_body_file}")"
pr_number="${pr_url##*/}"

gh issue comment "${issue_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）により実装し、PRを作成しました: ${pr_url}"
gh pr comment "${pr_number}" --repo "${REPO}" --body "夜間バッチ（ローカル実行）により実装しました。issue #${issue_number} を参照してください。"

trap - ERR
log "PR #${pr_number} を作成しました: ${pr_url}"

cd "${REPO_ROOT}"
git worktree remove --force "${worktree_dir}" 2>/dev/null || true
