# GitHub Actions ワークフロー詳細

- `on-pull-request.yml`: PR 作成・更新時に静的解析・テストを実行（`detekt` ジョブとは別に、ktlint（コードスタイル）を検証する `ktlint` ジョブ = `./gradlew ktlintCheck` を実行）。同一 PR に新しいコミットが追加された場合は、実行中の古い CI をキャンセルする。`desktop-screenshot-test-verify` / `android-screenshot-test-verify`（`_screenshot-test-verify.yml` を呼び出す）は、まず verify を実行し、失敗した場合のみ golden 画像を再記録してコミット・プッシュしたうえでジョブを失敗させる
- `on-main-merge.yml`: main へのマージ時に実行。`detekt` ジョブとは別に、ktlint（コードスタイル）を検証する `ktlint` ジョブ = `./gradlew ktlintCheck` を実行する。`dokka-pages` ジョブは Dokka（`./gradlew :dokkaGenerate`）で API ドキュメントを生成し、GitHub Pages（`github-pages` environment）へ自動デプロイする。ドキュメント本体（`docs/api/`）はコミットせず、CI 実行のたびに再生成する
- `_build-android-release.yml`: 署名付き Android APK をビルドする再利用可能ワークフロー（`workflow_call` 専用、単体では実行不可）。ファイル名・表示名を `_` で始め、Actions の実行一覧では手動起動対象として表示されないようにしている。`ref` 入力でビルド対象のブランチ・タグ・コミットを指定する。`build-apps.yml` と `release-apps.yml` の両方から呼び出される
- `_build-windows-msi.yml`: Windows MSI をビルドする再利用可能ワークフロー（`workflow_call` 専用、単体では実行不可）。`_build-android-release.yml` と同様に `ref` 入力でビルド対象を指定し、`gradle.properties` の `appVersion` を出力する。`build-apps.yml` と `release-apps.yml` の両方から呼び出される
- `build-apps.yml`: `workflow_dispatch` で起動し、Android APK と Windows MSI を並列にビルドする。Android APK のビルドは `_build-android-release.yml`、Windows MSI のビルドは `_build-windows-msi.yml` を呼び出す
- `release-apps.yml`: 手動でリリースする際に実行。まず `_e2e-android-maestro.yml`（`ref: main`）を実行し、成功した場合のみバージョンバンプ・MSI/APK ビルド・リリース作成に進む。バージョンバンプ後、`generate-baseline-profile` ジョブが Android エミュレータ上で `./gradlew :app:androidApp:generateReleaseBaselineProfile` を実行して `baseline-prof.txt` を再生成し artifact としてアップロードし、`commit-baseline-profile` ジョブがそれをダウンロードして差分があれば main へコミット・プッシュする（差分がなければコミットしない）。ジョブを分けているのは、main へ push 可能な `GH_PAT` を、サードパーティ Action（`reactivecircus/android-emulator-runner`）や任意の Gradle ビルド実行と同じジョブに同居させない（該当コードが侵害された場合の `GH_PAT` 漏洩・不正 push を避ける）ため。両ジョブとも `permissions` を明示（`generate-baseline-profile` は `contents: read` のみ、`commit-baseline-profile` は `contents: write`）し、`commit-baseline-profile` の `checkout` は `persist-credentials: false` としたうえで push 時のみ `GH_TOKEN` を使って認証ヘッダを都度指定する（zizmor の `artipacked`: チェックアウトした認証情報がジョブ内に永続化されたままになるリスクを避けるため）。Android APK のビルド（`_build-android-release.yml`）もこの再生成後の main を対象に `permissions: contents: read` で実行され、Windows MSI のビルド（`_build-windows-msi.yml`）はバージョンバンプ後すぐに並行して実行される
- `_e2e-android-maestro.yml`: `_build-android-release.yml` で署名付き APK をビルドし、Android エミュレータ上で Maestro（`.maestro/tap-bottom-tabs.yaml`）を実行してボトムナビゲーションの各タブ（ルール・ログ・その他）をタップする E2E テスト。`release-apps.yml` から呼び出されるほか、Actions の画面から `ref` を指定して手動実行できる
- 夜間実装バッチは GitHub Actions ではなく、ユーザーのローカル Mac 上で `scripts/nightly-implement-local.sh` を launchd 経由で定期実行する方式に移行した（旧 `nightly-implement.yml` は削除、2026-09-01）。詳細は「[夜間実装バッチ（ローカル実行）](#夜間実装バッチローカル実行)」を参照
- `nightly-todo.yml`: 毎日 JST 午前5時ごろ（`workflow_dispatch` でも手動実行可）に起動する。JST の日付・曜日をシェル側で明示的に算出したうえで、Claude Code CLI（`CLAUDE_CODE_OAUTH_TOKEN` シークレットで認証、Pro/Max サブスクリプション枠を使用）に `docs/nightly-todo-list.md` の「毎晩実行する項目」とその曜日の「曜日ローテーション項目」を調査させ、`docs/improvement-ideas.md` の編集権限のみを与える（`Edit`/`Write` ツールを同ファイルに限定し、`git`/`gh` 操作の権限は与えない）。追記前には `docs/resolved-improvement-ideas.md`（対応済み改善案の1行ログ、読み取り専用）も確認させ、過去に対応済みの内容を重複して追記しないようにする。Claude Code の最終出力（各項目の確認内容・追記有無の判断理由）は `nightly-todo-summary.txt` に保存され、下書き PR の説明欄に転記される（改善案の追記が無かった夜でも、何を調査してなぜ追記しなかったかを PR 上で確認できるようにするため）。Claude Code 実行後、ワークフロー側で `docs/improvement-ideas.md` 以外が変更されていないことを検証してから、ブランチ作成・コミット・プッシュ・下書き PR 作成をワークフローの固定処理として行う（`docs/improvement-ideas.md` の変更は事前承認が必要という CLAUDE.md のルールを守るため、直接 main には書き込まない）。GitHub への操作には `GH_PAT` シークレットを使用する（`GITHUB_TOKEN` で作成した PR は後続の CI ワークフローをトリガーしないため）

### 定期実行系機能の使い分け

KoDriverの夜間バッチ・自動化フローでは、Claude Code自体が持つ `/goal`（完了条件駆動）・`/loop`（時間駆動）・`Cron`（`CronCreate` によるセッション内スケジューラ）・`Workflow`（複数エージェント協調）はいずれも使用せず、GitHub Actions の cron（`nightly-todo.yml`）とユーザーのローカル Mac 上の `launchd`（`nightly-implement-local.sh`、詳細は次節）に統一している。

- `/goal` ・`/loop` ・`Workflow`: いずれも対話セッションに紐づく機能であり、無人・定期実行が前提の夜間バッチとは実行モデルが合わないため採用していない。
- `Cron`（`CronCreate`）: セッション起動中のみ発火し、セッション終了で消える・7日で自動失効するため、無人の夜間バッチの置き換えには使わない（詳細は次節「[夜間実装バッチ（ローカル実行）](#夜間実装バッチローカル実行)」の「移行した理由・制約」を参照）。
- 暴走時のキルスイッチ: GitHub Actions 側の `nightly-todo.yml` は、今後のスケジュール実行や手動起動を停止するには GitHub の Actions 画面からワークフローを無効化（Disable workflow）し、すでに実行中の処理を停止するには対象の run で `Cancel workflow` を実行する（Disable workflow は今後の起動を抑止するのみで、実行中のジョブはキャンセルしない）。ローカル `launchd` 側（`nightly-implement-local.sh`）は `launchctl bootout gui/$(id -u)/local.kodriver.nightly-implement` で即座に停止できる（詳細は次節「登録解除する場合」を参照）。`CLAUDE_CODE_DISABLE_CRON=1` 等の環境変数によるキルスイッチは、`Cron`（`CronCreate`）自体を使っていないため対象外。
- トークン消費監視: `nightly-todo.yml` は `CLAUDE_CODE_OAUTH_TOKEN`（Pro/Max サブスクリプション枠）を使用する。`nightly-implement-local.sh` はローカル `claude` CLI の認証設定に依存するため、課金形態を断定しない。想定外の実行量になっていないかは、認証設定に応じて `/usage`（Claude Code CLI）または該当する請求画面で確認する。

## 夜間実装バッチ（ローカル実行）

旧 `nightly-implement.yml`（GitHub Actions版）は削除し、`scripts/nightly-implement-local.sh` をユーザーのローカル Mac から launchd で定期実行する方式に置き換えた（2026-09-01）。ロジック自体（`claude-implementable` ラベル付きの最古issueを選定し、専用ワークツリーで Claude Code CLI に実装させ、`./gradlew preSubmitChecks` 通過後にコミット・プッシュ・PR作成する一連の流れ）は旧ワークフローと同一で、実行主体を GitHub Actions ランナーからローカル Mac に変えただけ。

1回の実行で処理するissue件数はスクリプト先頭の `MAX_ISSUES_PER_RUN`（デフォルト2件）で制御する。`claude-implementable` ラベル付きの未ブロックissueを番号順に最大でこの件数だけ選び、issueごとに独立したワークツリー・ブランチで上記の一連の流れ（実装→`preSubmitChecks`→コミット・プッシュ・PR作成）を順番に実行する。あるissueの処理が保留・失敗しても、後続のissueの処理は継続する。

### 移行した理由・制約

このリポジトリの `CronCreate`（Claude Codeセッション内のスケジューラ）はセッション起動中のみ発火し、セッション終了で消える・7日で自動失効するため、無人の夜間バッチの置き換えには使わず、macOS 標準の `launchd` を使う。launchd はセッションに依存せず、Mac の電源が入っていれば動作する（スリープ中に発火時刻を過ぎた場合はその回はスキップされる。次回の予定時刻には動く）。

### 初回セットアップ（ユーザーがローカル Mac で1回だけ実行）

1. `scripts/launchd/local.kodriver.nightly-implement.plist` をコピーし、`REPO_ROOT` をこのリポジトリの絶対パスに書き換える。
   ```bash
   cp scripts/launchd/local.kodriver.nightly-implement.plist ~/Library/LaunchAgents/local.kodriver.nightly-implement.plist
   sed -i '' "s#REPO_ROOT#$(pwd)#" ~/Library/LaunchAgents/local.kodriver.nightly-implement.plist
   ```
2. `claude` CLI・`gh` CLI がローカルで認証済み（`gh auth status` が通ること）であることを確認する。
3. launchd に登録する。
   ```bash
   launchctl bootstrap gui/$(id -u) ~/Library/LaunchAgents/local.kodriver.nightly-implement.plist
   ```
4. ログは `/tmp/kodriver-nightly-implement.log` に出力される。

登録解除する場合は `launchctl bootout gui/$(id -u)/local.kodriver.nightly-implement` を実行し、plist ファイルを削除する。

`scripts/nightly-implement-local.sh` は単独でも `./scripts/nightly-implement-local.sh` として手動実行できる（動作確認・デバッグ用途）。
