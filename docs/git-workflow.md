# Git 操作ルール

- **feature ブランチでのコミット・プッシュ・PR の作成は、ユーザーの明示的な指示を待たずに自発的に実行してよい。** `start-implementation` などのフローで実装が一区切りついた時点で、コミット・プッシュ・PR作成まで自動的に進めること。
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。** これは feature ブランチの自動化ルールの例外として維持する。
- **作業用ワークツリーは、必ずこのリポジトリの `.claude/worktrees/` 配下に作成すること。** リポジトリ外やその他のディレクトリに作成してはならない。
  - 例: `git worktree add .claude/worktrees/<worktree-name> -b <branch-name>`
- **ワークツリーを作成したら、リポジトリルートの `local.properties` から `sdk.dir` の行だけをワークツリーへコピーすること。** `local.properties` はマシン固有で git 管理外のため新規ワークツリーには存在せず、コピーしないと `./gradlew preSubmitChecks` が「SDK location not found」で失敗する。一方、同ファイルには `app/androidApp/build.gradle.kts` が読む署名鍵の秘匿情報（`STORE_PASSWORD` 等）も含まれるため、ファイルごとは複製しない（Claude・Codex からワークツリー内で読めてしまうため）。
  - 例: `grep '^sdk\.dir=' <リポジトリルート>/local.properties > .claude/worktrees/<worktree-name>/local.properties`
  - `scripts/nightly-implement-local.sh` も同じ方針でコピーしている。
  - リポジトリルートに `local.properties` がない場合はコピーしない（`ANDROID_HOME` 等で SDK を解決している環境）。
- **ワークツリーの削除は、自分のセッションで作成したものだけに限定すること。** 複数の Claude セッションが並行してワークツリーを使用している場合があるため、他のワークツリーは削除してはならない。
- **マージ済み PR のワークツリー・ブランチを片付ける際は、ローカルブランチだけでなくリモートブランチ（`origin/<ブランチ名>`）も削除すること。** 既にリモートブランチが存在しない（GitHub 側の自動削除等）場合はエラーを無視してよい。
- **PR のタイトルと説明は日本語で書くこと。**
- **PR の説明欄に「Generated with Claude Code」などの署名やセッション URL（`https://claude.ai/code/session_...`）を含めないこと。**
- **`feature/base/` 系のベースブランチ向け PR には、取り込み済み PR を記録する専用セクションを設けること。** そのベースブランチへ feature ブランチをプッシュ・マージした際、または `update-pr-description` を実行した際は、説明欄末尾の「## 取り込み済みPR」セクションに `- <PRタイトル>: #<番号>` の形式で1行追記する。フルURLを貼るとGitHubがリンクカードとして展開しPRタイトルが二重に表示されるため、番号参照（`#<番号>`）のみを使うこと。詳細な変更内容は各 PR 自体の説明に任せ、ベース PR 側の説明欄はタイトルと番号参照の一覧に留めて肥大化を防ぐ。
- **クラウド実行環境やワークツリーでは、コミット作成前に Git の作者情報を必ず確認すること。** Agent ツールの isolation が `remote` の場合などのクラウド実行環境やワークツリーでは、`git config user.name` と `git config user.email` を確認し、ハンドルネームや GitHub の noreply メールアドレスなど、意図した値であることを確かめる。クラウドエージェントがアカウントの本名を作者情報に自動設定し、公開リポジトリで本名が露出した事例がある。force push 後も古い SHA から変更前の内容を辿れるため、完全な削除は難しい。意図しない本名や個人のメールアドレスが設定されている場合は、コミットを作成せずユーザーに報告すること。参考: [Codex Cloudを利用してコード修正してもらっていたら本名が駄々洩れしていた話](https://blog.hitsujin.jp/entry/2026/10/05/codex-cloud-git-author)。
- **モジュール図・スクリーンショットテストの画像は `git add` してはならない。** `assertModuleGraph` が生成するモジュール図（例: `docs/graphs/*.gv`, `docs/graphs/*.svg`）や、スクリーンショットテストが生成・更新するスクリーンショット画像（例: `**/snapshots/*.png`）は CI で自動更新される仕組みのため、手元での変更をコミットすると CI の更新と競合する。動作確認のために生成されることがあるが、**ステージングすること自体を禁止する**。ファイルを指定してステージングするときは、これらのファイルを絶対に含めないこと。また、動作確認でこれらのファイルが生成・変更された場合は、**報告前に必ず `git checkout -- <file>` または `git clean -f <file>` で変更を破棄すること**。ただし、ユーザーから古いスクリーンショットテストや不要になったゴールデン画像の削除を明示的に指示された場合に限り、既存の `**/snapshots/*.png` の削除はステージングしてよい。

## moduleGraphAssert の変更禁止

`build.gradle.kts` の `moduleGraphAssert { ... }` ブロックは、ClaudeCode / Codex が自律的に変更してはならない。

- ユーザーが明示的に `moduleGraphAssert` の変更を指示した場合のみ変更してよい。
- モジュール追加・依存関係修正・CI 修正の一環であっても、事前確認なしに `allowed` / `restricted` / `configurations` を変更してはならない。
- `assertModuleGraph` が失敗した場合は、まず依存関係やモジュール構成側を修正し、`moduleGraphAssert` の緩和で解決しない。
