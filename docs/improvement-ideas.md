# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  **課題**: <現状の問題・気になっている点>
  **改善案**: <どう変えたいか>
```

---

## 設計・アーキテクチャ

- **対象**: `ReadoutNavigationState.kt`（`feature:readout-list`）・`OtherNavigationState.kt`（`app:shared`）・`TelemetryLogNavigationState.kt`（`feature:telemetry-log-list`）
  **課題**: list/detailペインの切り替え状態を`NavBackStack<NavKey>`で保持しているが、`clear()`→`add()`による「1要素の置き換え」としてのみ使っており、Navigation3本来の想定（`NavDisplay`によるレンダリング、pushによる複数エントリの積み上げ、戻る操作での自動pop）は利用していない。実際の画面遷移制御はMaterial3 Adaptiveの`rememberListDetailPaneScaffoldNavigator`/`ListDetailPaneScaffoldRole`が担っており、`NavBackStack`はそれと並行して「現在どちらのペインを表示しているか」を表す状態変数として存在するのみ。
  **改善案**: Navigation3のサンプル・公式ドキュメントにあるMaterial3 AdaptiveとNavDisplayの統合パターン（両者で単一のバックスタックを共有する設計）への寄せ替えを検討する。ただし現状の実装（PR #1069, #1075, #1077, #1078）で機能的な不具合は出ていないため、優先度は低め。
  **調査結果（2026-08-14）**: 統合用ライブラリ`org.jetbrains.compose.material3.adaptive:adaptive-navigation3`（AndroidX本家の`ListDetailSceneStrategy`に相当、`rememberListDetailSceneStrategy()`をNavDisplayに渡す構成）はJetBrains公式ドキュメント（https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html）に記載されており存在する。ただし現時点のバージョンは`1.3.0-beta02`で、プロジェクトが依存している`adaptive-layout`/`adaptive-navigation`の安定版`1.2.0`系とは異なるベータ系列。CLAUDE.mdの「致命的なバグや互換性問題がない限り最新安定版を使用する」方針とも相性が悪いため、この統合ライブラリが安定版としてリリースされてから改めて移行を検討する。

## UI/UX

- **対象**: `ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）の `ListDetailPaneScaffold`／画面幅判定まわり
- **課題**: Jetpack Compose 2026年4月リリース（Compose 1.11.0系）で追加された宣言的な `MediaQuery` API（`WindowSizeClass` の手動購読・分岐に代わり、ウィンドウ状態に応じた宣言的なクエリ記述が可能）をまだ利用していない。現状は `rememberListDetailPaneScaffoldNavigator` 等の既存の分岐ロジックで賄っている。
- **改善案**: プロジェクトが依存する Compose Multiplatform / Material3 Adaptive のバージョンで `MediaQuery` API が利用可能になった際、list/detailペインの表示切り替え判定を簡潔化できないか調査する。参考: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
- **調査結果（2026-08-23）**: プロジェクトが依存する `org.jetbrains.compose.ui:ui` `1.11.1` には `MediaQuery` API（`androidx.compose.ui.MediaQueryKt`、`derivedMediaQuery`、`UiMediaScope`、`LocalUiMediaScope`）が `commonMain` として同梱されており、`feature:readout-list` 等でのコンパイル自体は成功する（JVM・Androidいずれのターゲットでも確認済み）。しかし実際に3画面へ導入して `feature:readout-list` のユニットテストを実行したところ、`LocalUiMediaScope` を明示的に提供しなかったテストが軒並み `IllegalStateException`（`CompositionLocal LocalUiMediaScope not present`）で失敗した。原因を調査した結果、`androidx.compose.ui:ui-android`（AARの`classes.jar`）には `LocalUiMediaScope` へ実際の値を供給するプラットフォーム実装（`androidx/compose/ui/adaptive/MediaQuery_androidKt.obtainUiMediaScope` 等）が存在する一方、`org.jetbrains.compose.ui:ui-desktop:1.11.1` の jar にはこの配線が一切含まれていないことを確認した。**つまり `MediaQuery` API は現時点で Android ターゲットのみ実用可能で、KoDriver の主要配布形態である Desktop（Windows MSI）では `LocalUiMediaScope` が誰からも提供されず実行時にクラッシュする。** Compose Multiplatform がDesktopターゲット向けの `UiMediaScope` プロバイダ実装を追加するまでは導入を見送る。

## Android

- **対象**: `app/androidApp/src/main/AndroidManifest.xml`（`android-targetSdk = "36"`、`gradle/libs.versions.toml`）
  **課題**: Android 16（API 36）以降、`targetSdk` 36以上のアプリがプライベートアドレス（例: `192.168.x.x`）へ接続する際は `android.permission.ACCESS_LOCAL_NETWORK`（dangerous権限、実行時リクエストが必要）がないと接続パケットが**エラーを返さず黙って破棄される**（ループバック`127.0.0.1`は対象外）。KoDriverのAndroidアプリは `:feature:other-server-ip-detail` で設定したLAN内KoDriverサーバー（`:server`、`0.0.0.0:8080`）へWebSocket接続する構成のため、`targetSdk = 36` の現状構成では、マニフェストに権限宣言・実行時リクエストがない場合Android 16実機でサイレントにタイムアウトし、原因特定が困難な接続不能バグとなるおそれがある。現在の `AndroidManifest.xml` には `INTERNET` 権限のみが宣言されている。
  **改善案**: `AndroidManifest.xml` に `<uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" android:minSdkVersion="36" />` を追加し、`ActivityResultContracts.RequestPermission()` 等でランタイム許可を取得する導線を検討する。実機がなくても `adb shell appops get <package> | grep -i local` で拒否状態を確認できる。
  **参考URL**: https://zenn.dev/ace_toshi/articles/android-access-local-network

## バグ

- **対象**: `feature/debug-state-detail/src/commonMain/composeResources/values/strings.xml` の `debug_state_tyre_wear_fl`（L69, `_fr`/`_rl`/`_rr`も同様に計4件）・`debug_state_fuel_consumption_per_lap_ratio`（L74）・`debug_state_fuel_consumption_remaining_percent`（L77）
  **課題**: `%1$s%` のように書式指定子の直後に単独の `%` を置いている。Android/Compose Resourcesの文字列フォーマット処理では、書式指定子中の `%` はエスケープ（`%%`）しないとフォーマット例外や表示崩れの原因になりうる。実際 `feature/ace-windows-readout-remaining-fuel-detail/src/commonMain/composeResources/values/strings.xml` の `remaining_fuel_threshold_label`（L8）は `%1$s%%` と正しくエスケープしており、モジュール間で書き方が不統一。
  **改善案**: `debug-state-detail` の該当4文字列リソースを `%1$s%%` 表記に修正し、実際に `stringResource` 経由でフォーマットして表示崩れ・例外が起きないことを確認する。

## セキュリティ

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt` の `hostNameProvider`（L19）・`sanitizedHostName()`（L36）
  **課題**: mDNS広告のサービスインスタンス名にOSのホスト名（`InetAddress.getLocalHost().hostName` をドメイン部分だけ除去したもの）をそのまま使用している。ユーザーが個人名を含むPC名（例: `Taro-PC`）を設定している場合、LAN内の第三者にその名前がmDNS経由で広告されてしまう。既存のホスト名サニタイズ（PR #610）はサービスタイプ重複やリーク対策が目的で、この個人情報露出は未検討。
  **改善案**: サービスインスタンス名をホスト名依存ではなくアプリ固有の識別子（例: 固定文字列+ランダムサフィックスや設定可能な表示名）に変更する、またはREADMEに「PC名がLAN内に広告される」ことを既知の制約として明記することを検討する。

## CI/CD

- **対象**: `app/desktopApp/build.gradle.kts` の `windows { }` ブロック(PR #1142)
  **課題**: `shortcut = true` / `menu = true` / `perUserInstall = true` はjpackageの仕様上いずれもサイレントフラグであり、インストール実行時に自動でその挙動が固定されるだけで、ユーザーに選択させるダイアログは表示されない。ショートカット作成可否を選ばせるには別途 `--win-shortcut-prompt` が必要だが、Compose MultiplatformのGradle DSL(`org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask`)には対応するプロパティが存在せず、現状のDSLの範囲では実現できない。インストール範囲(全ユーザー/個人用)を選ばせる標準ダイアログもjpackage自体に用意されていない。
  **改善案**: Compose Multiplatformが `winShortcutPrompt` 等のDSLプロパティを将来追加した場合、または freeform引数差し込み等の代替手段が判明した場合に、MSIインストーラー上でショートカット作成可否をユーザーに選択させる機能の追加を検討する。

## 開発体験

- **対象**: `app/desktopApp` のホットリロード開発フロー（`./gradlew :app:desktopApp:hotRun --auto`）
  **課題**: Compose Multiplatform 1.12.0でCompose Hot Reloadに「MCP server for AI agents」が追加され、AIコーディングエージェントが実行中アプリに接続してリロードのトリガー・スクリーンショット取得・UI検査・クリック/テキスト入力のシミュレーション・ログ読取が可能になった（参考: https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/）。KoDriverのUI変更確認は現状目視・スクリーンショットテストに依存しており、この仕組みを使えばClaude Code自身がホットリロード中のデスクトップアプリを直接操作・検証できる可能性がある。
  **改善案**: プロジェクトが依存するCompose Multiplatformのバージョンを1.12.0系へ更新するタイミングで、Hot ReloadのMCP serverを実際に有効化し、`preSubmitChecks` 前の手動UI確認フローに組み込めないか調査する。

- **対象**: `app/desktopApp` のウィンドウ・ダイアログ生成部分
  **課題**: Compose Multiplatform 1.12.0でWindow/DialogStateのv2 APIが追加され、画面選択・カスタム位置/サイズロジック・ウィンドウサイズの最小/最大設定・ダイアログの親ウィンドウ相対配置が可能になった（参考: https://blog.jetbrains.com/kotlin/2026/08/compose-multiplatform-1-12-0/）。現状KoDriverのデスクトップウィンドウ・各種設定ダイアログでこれらの制御が必要になった際の実装手段が不明瞭。
  **改善案**: プロジェクトが依存するCompose Multiplatformのバージョンを1.12.0系へ更新した際、既存のウィンドウ/ダイアログ生成コードでv2 APIへの置き換えが有用な箇所がないか調査する。

