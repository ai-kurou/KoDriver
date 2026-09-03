# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  - **課題**: <現状の問題・気になっている点>
  - **改善案**: <どう変えたいか>
```

---

## 設計・アーキテクチャ

- **対象**: `ReadoutNavigationState.kt`（`feature:readout-list`）・`OtherNavigationState.kt`（`app:shared`）・`TelemetryLogNavigationState.kt`（`feature:telemetry-log-list`）
  - **課題**: list/detailペインの切り替え状態を`NavBackStack<NavKey>`で保持しているが、`clear()`→`add()`による「1要素の置き換え」としてのみ使っており、Navigation3本来の想定（`NavDisplay`によるレンダリング、pushによる複数エントリの積み上げ、戻る操作での自動pop）は利用していない。実際の画面遷移制御はMaterial3 Adaptiveの`rememberListDetailPaneScaffoldNavigator`/`ListDetailPaneScaffoldRole`が担っており、`NavBackStack`はそれと並行して「現在どちらのペインを表示しているか」を表す状態変数として存在するのみ。
  - **改善案**: Navigation3のサンプル・公式ドキュメントにあるMaterial3 AdaptiveとNavDisplayの統合パターン（両者で単一のバックスタックを共有する設計）への寄せ替えを検討する。ただし現状の実装（PR #1069, #1075, #1077, #1078）で機能的な不具合は出ていないため、優先度は低め。
  - **調査結果（2026-08-14）**: 統合用ライブラリ`org.jetbrains.compose.material3.adaptive:adaptive-navigation3`（AndroidX本家の`ListDetailSceneStrategy`に相当、`rememberListDetailSceneStrategy()`をNavDisplayに渡す構成）はJetBrains公式ドキュメント（https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html）に記載されており存在する。ただし現時点のバージョンは`1.3.0-beta02`で、プロジェクトが依存している`adaptive-layout`/`adaptive-navigation`の安定版`1.2.0`系とは異なるベータ系列。CLAUDE.mdの「致命的なバグや互換性問題がない限り最新安定版を使用する」方針とも相性が悪いため、この統合ライブラリが安定版としてリリースされてから改めて移行を検討する。

## UI/UX

- **対象**: `ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）の `ListDetailPaneScaffold`／画面幅判定まわり
  - **課題**: Jetpack Compose 2026年4月リリース（Compose 1.11.0系）で追加された宣言的な `MediaQuery` API（`WindowSizeClass` の手動購読・分岐に代わり、ウィンドウ状態に応じた宣言的なクエリ記述が可能）をまだ利用していない。現状は `rememberListDetailPaneScaffoldNavigator` 等の既存の分岐ロジックで賄っている。
  - **改善案**: プロジェクトが依存する Compose Multiplatform / Material3 Adaptive のバージョンで `MediaQuery` API が利用可能になった際、list/detailペインの表示切り替え判定を簡潔化できないか調査する。参考: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
  - **調査結果（2026-08-23）**: プロジェクトが依存する `org.jetbrains.compose.ui:ui` `1.11.1` には `MediaQuery` API（`androidx.compose.ui.MediaQueryKt`、`derivedMediaQuery`、`UiMediaScope`、`LocalUiMediaScope`）が `commonMain` として同梱されており、`feature:readout-list` 等でのコンパイル自体は成功する（JVM・Androidいずれのターゲットでも確認済み）。しかし実際に3画面へ導入して `feature:readout-list` のユニットテストを実行したところ、`LocalUiMediaScope` を明示的に提供しなかったテストが軒並み `IllegalStateException`（`CompositionLocal LocalUiMediaScope not present`）で失敗した。原因を調査した結果、`androidx.compose.ui:ui-android`（AARの`classes.jar`）には `LocalUiMediaScope` へ実際の値を供給するプラットフォーム実装（`androidx/compose/ui/adaptive/MediaQuery_androidKt.obtainUiMediaScope` 等）が存在する一方、`org.jetbrains.compose.ui:ui-desktop:1.11.1` の jar にはこの配線が一切含まれていないことを確認した。**つまり `MediaQuery` API は現時点で Android ターゲットのみ実用可能で、KoDriver の主要配布形態である Desktop（Windows MSI）では `LocalUiMediaScope` が誰からも提供されず実行時にクラッシュする。** Compose Multiplatform がDesktopターゲット向けの `UiMediaScope` プロバイダ実装を追加するまでは導入を見送る。

## テスト

- **対象**: `feature:readout-list`（`ReadoutDetailPane.kt`）
  - **課題**: `if (canNavigateBack)` による戻るボタンの非表示パターンが、通常のUIテスト・スクリーンショットテストのいずれでも未検証。そもそもこのComposable単体のスクリーンショットテスト自体が存在しない。
  - **改善案**: `canNavigateBack = false` のケースを含むスクリーンショットテストを新規に整備する。

- **対象**: `core:designsystem`（`DetailPane.kt` の `DetailPaneSubtitle`）
  - **課題**: `if (trailingContent != null)` による表示分岐について、`DetailPaneSubtitle` 単独のスクリーンショットテストが存在しない。呼び出し側のテストで間接的に一部カバーされているのみで、`trailingContent`有無の対比検証はない。
  - **改善案**: `DetailPaneSubtitle` 単独のスクリーンショットテストを新設し、`trailingContent`の有無双方のケースを追加する。

- **対象**: `feature/other-list/src/commonMain/kotlin/kurou/kodriver/feature/otherlist/OtherListPane.kt:313`
  - **課題**: `LazyColumn` ビルダーラムダ内で `val groupedItems = uiState.items.groupBy { it.section() }` を `remember` なしで実行している。`uiState`（`collectAsState` 由来）は `keepScreenOn`・ダイナミックカラー・ハプティクス等どの設定が変わっても更新されるため、`uiState.items` 自体に変化がなくても毎回再グルーピングが走る。
  - **改善案**: `remember(uiState.items) { uiState.items.groupBy { it.section() } }` のように `items` をキーにした `remember` でラップし、無関係な状態変化での再計算を避ける。

- **対象**: `feature/telemetry-log-list/src/commonMain/kotlin/kurou/kodriver/feature/telemetryloglist/TelemetryLogListPane.kt:163-165`
  - **課題**: `items(items = uiState.logs, key = { it.id })` のループボディ内で `onClick = { onLogClick(log.id) }` 等、`log` をキャプチャする3つのラムダを `remember` せず直書きしている。呼び出し先の `TelemetryLogListItem` は選択状態変化時に `animateColorAsState` で再コンポジションされるため、そのたびにこれら3ラムダが再生成される。
  - **改善案**: `remember(log.id) { { onLogClick(log.id) } }` 等でラップするか、`onClick` を `log.id` を引数に取る安定したコールバック形へ変更し、不要な再生成を避ける。

- **対象**: `compose-state-and-effects` スキルが対象とする各画面の `LaunchedEffect` 使用箇所全般（例: 一覧/詳細ペインでの一度きりの副作用実行）
  - **課題**: Jetpack Compose 1.12で `SideEffect` にキー引数（`SideEffect(keys) { ... }`）が追加され、suspend不要・後片付け不要な「一度だけ実行し、キー変更時のみ再発火する」処理について、従来 `LaunchedEffect` を代用していたケースをより軽量なAPIに置き換えられるようになった（記事によれば`LaunchedEffect`比で最大90%高速とされる）。ただしレイアウト確定前に実行される点、キー変更時に「前回分の取り消し」ができない点（撃ちっぱなしのログ記録等のみ対象）に注意が必要。KoDriverが依存する `composeMultiplatform`（現状1.11.1）がこのAPIを含む1.12系へ追随した際に、該当する`LaunchedEffect`使用箇所を洗い出す余地がある。
  - **改善案**: `composeMultiplatform`を1.12系へ更新するタイミング（`docs/improvement-ideas.md`「開発体験」節のHot Reload MCP server化・material3/material3-adaptive追随待ちの項目と合わせて検討）で、`compose-state-and-effects`スキルの対象範囲を`rg`等で確認し、suspend・後片付けが不要な`LaunchedEffect`が`SideEffect(keys)`へ置き換えられないか調査する。
  - **参考URL**: https://zenn.dev/uphyca/articles/00f497f365cc1e

## Android

- **対象**: `:feature:other-server-ip-detail`、`server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`（mDNS広告 `_kodriver._tcp.local.`）
  - **課題**: Android 17でGoogleが新設した「ローカルネットワーク保護」により、アプリが同一LAN上の他デバイスをスキャン（mDNS探索を含む）する際にユーザーの明示的な許可が必要になった（参考: https://blog.google/security/new-Android-network-security-protections/ )。`:feature:other-server-ip-detail` はKoDriverサーバーが広告するmDNSサービスを検出して接続先IPを自動入力する機能を持つため、Android 17端末では権限ダイアログの表示・拒否時のフォールバック挙動（手動IP入力への案内など）の対応が必要になる可能性がある。上記のAndroid 16向け `ACCESS_LOCAL_NETWORK` 権限との関係・重複の有無も含めて未調査。
  - **改善案**: Android 17実機またはエミュレータでmDNS自動検出フローを検証し、権限ダイアログが表示されるか、拒否時に自動検出が失敗した場合のUI（エラーメッセージ・手動入力への誘導）が適切かを確認する。必要であれば権限リクエスト導線・フォールバックUIを追加する。

## バグ

- **対象**: `feature/gt7-ps5-readout-remaining-fuel-laps-detail/src/commonMain/composeResources/values/strings.xml:3`（`remaining_fuel_laps_description`）・`feature/lmu-windows-readout-pit-timing-detail/src/commonMain/composeResources/values/strings.xml:3`（`pit_timing_description`）
  - **課題**: どちらも「直近のベストラップの30秒前のタイミングで残り燃料/エナジーを判定する」という同一仕様を説明しているが、GT7版は「現在の**最速ラップ**の30秒前」、LMU版は「毎周**ベストラップ**の30秒前」と用語が不統一。他の箇所（`my_best_lap`系、`debug_state_best_lap_title`、`readout_item_my_best_lap`等）は一貫して「ベストラップ」「自己ベストラップ」表記に統一されており、`remaining_fuel_laps_description`の「最速ラップ」だけが用語として孤立している。
  - **改善案**: `remaining_fuel_laps_description` の「最速ラップ」を「ベストラップ」に統一する。

- **対象**: `feature/debug-state-detail/src/commonMain/composeResources/values/strings.xml` の `debug_state_tyre_wear_fl`（L69, `_fr`/`_rl`/`_rr`も同様に計4件）・`debug_state_fuel_consumption_per_lap_ratio`（L74）・`debug_state_fuel_consumption_remaining_percent`（L77）
  - **課題**: `%1$s%` のように書式指定子の直後に単独の `%` を置いている。Android/Compose Resourcesの文字列フォーマット処理では、書式指定子中の `%` はエスケープ（`%%`）しないとフォーマット例外や表示崩れの原因になりうる。実際 `feature/ace-windows-readout-remaining-fuel-detail/src/commonMain/composeResources/values/strings.xml` の `remaining_fuel_threshold_label`（L8）は `%1$s%%` と正しくエスケープしており、モジュール間で書き方が不統一。
  - **改善案**: `debug-state-detail` の該当4文字列リソースを `%1$s%%` 表記に修正し、実際に `stringResource` 経由でフォーマットして表示崩れ・例外が起きないことを確認する。

- **対象**: `core/data/src/jvmAndroidMain/kotlin/kurou/kodriver/data/release/GitHubAppReleaseRepository.kt`（L34-38, L53-55）・`feature/other-server-ip-detail/src/commonMain/kotlin/kurou/kodriver/feature/otherserveripdetail/SaveServerIpWithConnectivityCheckUseCase.kt`（L31-35）・`feature/other-feedback-detail/src/commonMain/kotlin/kurou/kodriver/feature/otherfeedbackdetail/OtherFeedbackDetailViewModel.kt`（L135-139）・`feature/other-console-ip-detail/src/commonMain/kotlin/kurou/kodriver/feature/otherconsoleipdetail/OtherConsoleIpDetailViewModel.kt`（L77-81）・`feature/telemetry-log-list/src/commonMain/kotlin/kurou/kodriver/feature/telemetryloglist/TelemetryLogListViewModel.kt`（L78-82, L129-133）
  - **課題**: これらは `catch (e: CancellationException) { throw e } catch (e: Exception) { ... }` の形で例外を捕捉した後、`Sentry.captureException` などの記録を一切行わず `null`／固定の失敗状態（`SaveFailed`・`FeedbackSendStatus.Failed`・`saveFailed = true`・`false`）へ握りつぶしている。同じリポジトリ内の `HttpServerVersionRepository`・`WebSocketFlowFactory`・`PreferencesSerializerFactory`・`Gt7Ps5UdpPortPreferencesSerializer` 等は同種の例外を `Sentry.captureException` で記録してから失敗として扱っており、この慣習から外れている。特に `TelemetryLogListViewModel` の対象操作はDB全削除・個別削除という破壊的操作で、失敗原因が完全に失われるとユーザー報告時の原因特定が困難になる。
  - **改善案**: 上記の各 `catch (e: Exception)` ブロックで、他のRepository/DataSourceと同様に `Sentry.captureException(e)`（または `core:narrator` の `captureNarratorError` に相当する共通ヘルパー）を呼んでから失敗状態へフォールバックするよう統一する。

- **対象**: `feature/other-console-ip-detail/src/commonMain/kotlin/kurou/kodriver/feature/otherconsoleipdetail/OtherConsoleIpDetailPane.kt`（L243）
  - **課題**: `saveFailed` 表示のエラーメッセージが `Text(text = "保存に失敗しました", ...)` とハードコードされた日本語文字列リテラルになっており、`composeResources`（`strings.xml`）を経由していない。同一モジュール内の `console_ip_port_33741_label`（L236）など他の文言は `stringResource` 経由で `strings.xml` から参照しており、この1箇所だけが慣習から外れている。リポジトリ全体を検索してもこの文字列リテラルは他にヒットせず、文字列リソース化の抜けになっている。
  - **改善案**: `feature/other-console-ip-detail/src/commonMain/composeResources/values/strings.xml` に `console_ip_save_failed`（仮称）等のキーで文字列リソースを追加し、`OtherConsoleIpDetailPane.kt:243` を `stringResource(Res.string.console_ip_save_failed)` に置き換える。

## CI/CD

- **対象**: `app/desktopApp/build.gradle.kts` の `windows { }` ブロック(PR #1142)
  - **課題**: `shortcut = true` / `menu = true` / `perUserInstall = true` はjpackageの仕様上いずれもサイレントフラグであり、インストール実行時に自動でその挙動が固定されるだけで、ユーザーに選択させるダイアログは表示されない。ショートカット作成可否を選ばせるには別途 `--win-shortcut-prompt` が必要だが、Compose MultiplatformのGradle DSL(`org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask`)には対応するプロパティが存在せず、現状のDSLの範囲では実現できない。インストール範囲(全ユーザー/個人用)を選ばせる標準ダイアログもjpackage自体に用意されていない。
  - **改善案**: Compose Multiplatformが `winShortcutPrompt` 等のDSLプロパティを将来追加した場合、または freeform引数差し込み等の代替手段が判明した場合に、MSIインストーラー上でショートカット作成可否をユーザーに選択させる機能の追加を検討する。
  - **調査結果（2026-08-27）**: Compose Multiplatform最新版（1.12.0、2026年8月リリース）のリリースノート・`AbstractJPackageTask`のソース（GitHub master）を確認したが、`winShortcutPrompt`等の専用DSLプロパティは依然として未追加。ただし`AbstractJPackageTask`には任意のjpackage引数をそのまま渡せる`freeArgs`プロパティが存在し、GitHub Issue #773（ファイル関連付け要望）のコメントでは`freeArgs`経由で`--file-associations`等の未サポート引数を注入するワークアラウンドが確認できる。理論上は`freeArgs.add("--win-shortcut-prompt")`のような形で本件も実現できる可能性が高いが、実際にWindows環境で動作するかは未検証。DSL標準サポートはまだ無いため、実機検証を行うか、DSLが対応するまで着手を見送る。

## 開発体験

- **対象**: `.claude/settings.json` の `hooks.PreToolUse`（Bashツール呼び出し前のガード）
  - **課題**: CLAUDE.mdの「実行するアクションの慎重さ」節では、破壊的な操作（`rm -rf`等）を実行前に確認するようエージェントの振る舞いとして定義しているが、誤った作業ディレクトリでの実行や変数展開ミスなど、モデル側の判断ミスに起因する事故を機械的に防ぐ仕組み（フックによる強制ガード）は導入されていない。
  - **改善案**: Zennで紹介されている、Bashツールの`rm`呼び出しをインターセプトし、`/`・ホームディレクトリ・`~/.ssh`等の保護対象パスを`deny`、それ以外の破壊的操作を`ask`で一時停止する`PreToolUse`フック（Python標準ライブラリのみで実装可能）を参考に、`.claude/settings.json`への導入余地を検討する。
  - **参考URL**: https://zenn.dev/gorizawa/articles/claude-code-guard-delete-hook

- **対象**: `app/desktopApp` のホットリロード開発フロー（`./gradlew :app:desktopApp:hotRun --auto`）
  - **課題**: Compose Multiplatform 1.12.0でCompose Hot Reloadに「MCP server for AI agents」が追加され、AIコーディングエージェントが実行中アプリに接続してリロードのトリガー・スクリーンショット取得・UI検査・クリック/テキスト入力のシミュレーション・ログ読取が可能になった（参考: https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/）。KoDriverのUI変更確認は現状目視・スクリーンショットテストに依存しており、この仕組みを使えばClaude Code自身がホットリロード中のデスクトップアプリを直接操作・検証できる可能性がある。
  - **改善案**: プロジェクトが依存するCompose Multiplatformのバージョンを1.12.0系へ更新するタイミングで、Hot ReloadのMCP serverを実際に有効化し、`preSubmitChecks` 前の手動UI確認フローに組み込めないか調査する。

- **対象**: `.claude/worktrees/` を使った並行ワークツリー運用（CLAUDE.md「Git 操作ルール」の「複数のClaudeセッションが並行してワークツリーを使用している場合がある」という前提）
  - **課題**: 現状のルールは「他セッションのワークツリー・ブランチを削除しない」という受動的な事故防止に留まっており、同一リポジトリを複数のAIエージェント（Claude Code・Codex等）が同時に操作する際に起こりうる `git pull`/`git push` 時のロックファイル残留による自滅ループや、セッションクラッシュ時の未コミット変更の放置については明文化されたルールがない。
  - **改善案**: Zennの実例（作業宣言ファイルによる担当範囲の3行宣言・セッション開始時の未コミット変更確認とロック掃除など）を参考に、CLAUDE.mdの「作業開始時」チェックリストへ「他セッションが同一箇所を作業中でないかの確認」「開始時のロックファイル・未コミット変更の確認」を追加する余地がないか検討する。
  - **参考URL**: https://zenn.dev/hilopon/articles/two-ai-git-ops-accident-prevention

- **対象**: 夜間バッチ（`nightly-todo.yml`）・`docs/nightly-todo-list.md` の運用
  - **課題**: 現状の夜間バッチは `/loop` 相当の定期実行の仕組み（GitHub Actionsのcron）に依存しているが、Claude Code自体が持つ `/goal`（完了条件駆動）・`/loop`（時間駆動）・Cron・Workflow（複数エージェント協調）の使い分けや、暴走時のキルスイッチ（`CLAUDE_CODE_DISABLE_CRON=1`等）・トークン消費監視（`/usage`）についてはドキュメント化されていない。
  - **改善案**: Qiitaの整理記事を参考に、KoDriverの夜間バッチ・自動化フローで各機能をどう使い分けているか（またはなぜ使わないか）を `docs/nightly-todo-list.md` や `docs/ci-workflows.md` に補足できないか検討する。
  - **参考URL**: https://qiita.com/NaokiIshimura/items/71af4e891b2f8f1e7943

- **対象**: `docs/ci-workflows.md`、夜間バッチ（`nightly-todo.yml`）、`explain-pr-feedback`/`fix-pr-feedback`スキル
  - **課題**: Claude Codeの「routine」機能を使うと、PRレビューやエラー調査といった定型タスクを定額コストで自動実行できる事例が紹介されている。KoDriverの夜間バッチ・PR指摘対応フローは現状Claude Code CLI呼び出し（GitHub Actions cron）ベースで、コスト・運用面でroutine化できる余地があるか未調査。
  - **改善案**: 夜間バッチ・PR指摘対応フローのうち、routine機能への置き換えでコスト最適化やレイテンシ改善が見込める箇所がないか調査する。
  - **参考URL**: https://zenn.dev/yutake27/articles/6be03483c0110b

- **対象**: `.claude/settings.json` / `.claude/settings.local.json`（現状リポジトリには未コミット。ローカル環境の許可設定が対象）の Bash/MCPツール許可ルール
  - **課題**: Zennの実例では、152件の許可ルールのうち完全一致（ワイルドカードなし）で書かれた92件が、10万9762回のツール実行で一度も発火していなかったことが実測で判明している。許可ルールは一度追加すると削除されにくく、コマンドの引数やパスが少し変わるだけで完全一致ルールは無効化されるため、「許可したつもりが実際には毎回確認ダイアログが出ている」状態に気づきにくい。KoDriverでは `fewer-permission-prompts` スキルで許可ルールの追加は行っているが、逆方向（一度も発火していない死んだルールの棚卸し）は運用に組み込まれていない。
  - **改善案**: `fewer-permission-prompts` スキルの運用に、追加だけでなく定期的な棚卸し（トランスクリプトや実行ログから実際に発火した許可ルールを集計し、一定期間発火していない完全一致ルールをワイルドカード化または削除する）の手順を補足できないか検討する。
  - **参考URL**: https://zenn.dev/tsutomusaito/articles/permission-rules-decay-ja

- **対象**: `app/desktopApp` のウィンドウ・ダイアログ生成部分
  - **課題**: Compose Multiplatform 1.12.0でWindow/DialogStateのv2 APIが追加され、画面選択・カスタム位置/サイズロジック・ウィンドウサイズの最小/最大設定・ダイアログの親ウィンドウ相対配置が可能になった（参考: https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/）。現状KoDriverのデスクトップウィンドウ・各種設定ダイアログでこれらの制御が必要になった際の実装手段が不明瞭。
  - **改善案**: プロジェクトが依存するCompose Multiplatformのバージョンを1.12.0系へ更新した際、既存のウィンドウ/ダイアログ生成コードでv2 APIへの置き換えが有用な箇所がないか調査する。
  - **調査結果（2026-08-28）**: `composeMultiplatform`のみを`1.12.0`へ単純更新したところ、Desktop/Androidのビルド自体は成功したが、`preSubmitChecks`で`OtherContentScreenshotTest`（`app:shared:jvmTest`）2件・`AppTest`（`app:desktopApp:test`、E2Eタップ順テスト）1件が`java.lang.IllegalStateException: Must run runDetachLifecycle() once after runAttachLifecycle() and before markAsDetached()`（`androidx.compose.ui.scene.BaseComposeScene.close`経由、テストシーンのdispose処理内部）で失敗した。原因を調査した結果、Compose Multiplatform 1.12.0がバンドルする組み合わせは`material3: 1.12.0-alpha03` / `material3-adaptive: 1.3.0-beta02`（GitHub Release Notes記載）だが、プロジェクトは`material3 = "1.11.0-alpha07"`・`adaptive-layout`/`adaptive-navigation = "1.2.0"`（1.11.1向けの組み合わせ）を個別ピン留めしているため、`compose-ui`だけ1.12.0にするとバージョン不整合が生じていた。試しに`material3`を`1.12.0-alpha03`、`adaptive-layout`/`adaptive-navigation`を`1.3.0-beta02`に揃えたところ、上記の失敗は解消することを確認した（対象テストのみ再実行、`BUILD SUCCESSFUL`）。ただし`material3`・`material3-adaptive`いずれも2026-08-28時点で安定版が存在せず（`material3-adaptive`の安定版は`1.2.0`が最新でCompose 1.9.3向け）、CLAUDE.mdの「致命的なバグや互換性問題がない限り最新安定版を使用する」方針との整合が取れないため、今回は1.12.0への更新を見送った。`material3`/`material3-adaptive`の安定版が1.12.0系に追随してリリースされた時点で、改めてバージョンを揃えた更新を検討する（バージョンがずれたまま`composeMultiplatform`だけ上げるとテストが壊れる点に注意）。
