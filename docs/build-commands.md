# ビルド・実行コマンド

```bash
# デスクトップアプリ起動（通常）
./gradlew :app:desktopApp:run

# デスクトップアプリ起動（ホットリロード）
./gradlew :app:desktopApp:hotRun --auto

# Ktor サーバー単体起動（共有メモリ由来のフラッグ情報は配信しない）
./gradlew :server:run

# Windows MSI パッケージビルド（CI: GitHub Actions / ローカル Windows 環境）
./gradlew :app:desktopApp:packageMsi

# Kover 対象モジュールのテストとカバレッジレポート生成
./gradlew koverXmlReport

# CI の unit-test と同様にスクリーンショットテストを除外してカバレッジレポート生成
# verifyRoborazzi* / recordRoborazzi* タスクを指定した場合は、このプロパティによる除外は行わない
./gradlew koverXmlReport -PexcludeScreenshotTests=true

# 完了報告・PR 作成前の必須チェック一式（全モジュールのdetekt・ktlintCheck・Android lint・
# Android 実機テスト（androidTest）のコンパイル・
# assertModuleGraph・assertArchitectureDocModuleList・全ユニットテスト（カバレッジ付き）・
# 両アプリのビルド・デスクトップ統合テスト）
./gradlew preSubmitChecks

# メモリ削減のため org.gradle.workers.max=2・Kotlin in-process・Gradleヒープ2GBに設定しており、その分実行時間は延びる。
# メモリに余裕がある環境では -Dorg.gradle.workers.max=N や ~/.gradle/gradle.properties で上書きできる。

# Vosk日本語モデルの取得（:app:androidApp・:app:desktopApp のビルドで自動実行される）
# 初回は配布元からの取得にネットワークが必要。zipはSHA-256検証のうえ Gradleユーザーホームの kodriver-vosk-model に
# キャッシュされ、次回以降は再取得しない。取得失敗時は間隔を空けて再試行する（build-logic の PrepareVoskModelTask）。
# CI でキャッシュを使いたい場合は ~/.gradle/kodriver-vosk-model を保存対象にする
./gradlew :app:desktopApp:prepareVoskModel

# 静的解析とモジュール依存関係の検証
./gradlew detekt assertModuleGraph

# ktlint（コードスタイル）チェック・自動整形
./gradlew ktlintCheck
./gradlew ktlintFormat

# Android・デスクトップアプリのビルドと統合テスト
./gradlew :app:androidApp:assembleDebug
./gradlew :app:desktopApp:jar
./gradlew :app:desktopApp:test

# 特定モジュールだけを確認する場合
./gradlew :<module-path>:jvmTest
```

GitHub Actions ワークフローの一覧・詳細な挙動・権限設計は [`docs/ci-workflows.md`](ci-workflows.md) を参照。

## preSubmitChecks を終了が確定できる形で起動する

`preSubmitChecks` はフォアグラウンド実行の上限（10分）を超えやすく、`run_in_background` の終了通知もGradleの子プロセスが残ると届かないことがある。終了を取りこぼして待機し続けないよう、ログと終了コードをファイルに残す形で起動する。

```bash
LOG=<スクラッチパッドのディレクトリ>/preSubmitChecks.log
rm -f "$LOG"
(./gradlew preSubmitChecks --console=plain > "$LOG" 2>&1; echo "EXIT=$?" >> "$LOG") &
```

- 終了の判定は、ログ末尾の `EXIT=<終了コード>` 行だけで行う。`EXIT=0` なら成功、それ以外は失敗として `BUILD FAILED` 周辺のログを読んで修正する。
- 待機は Monitor の until ループで `EXIT=` 行の出現を待ち、上限時間を付ける（例: 60分）。`grep -q '^EXIT=' "$LOG"` が成立するまで一定間隔で確認する。
- 待機のタイムアウトは終了ではない。タイムアウトしたら `ps aux | grep -i gradle`・`./gradlew --status`・ログの最終更新時刻（`stat`）で状態を確認し、更新が数分以上止まっていればハングとみなして、該当プロセスを止めて再実行する。実行中の場合は同じログを引き続き待つ（`preSubmitChecks` を重ねて起動しない）。
- ハングの原因は、ログ末尾の実行中タスク名や `jstack <テストワーカーのPID>` で特定する。

作業中に個別のチェックを素早く回したい場合は、以下も利用できる（完了報告前の `preSubmitChecks` 実行は省略不可）。

```bash
# 変更したモジュールのテストだけを実行（例: feature:auto-readout-list を変更した場合）
./gradlew :feature:auto-readout-list:jvmTest

# server モジュールを変更した場合
./gradlew :server:test

# androidMain に変更がある場合は androidHostTest も実行（例: core:data を変更した場合）
./gradlew :core:data:testAndroidHostTest
```

detekt の閾値設定は `config/detekt/detekt.yml` を参照（`MagicNumber` は無効化済みで数値リテラルは許容、`@Composable` は `LongMethod` の対象外）。

## preSubmitChecks に含まれるチェック

- 全モジュールの detekt（モジュール単位の `:xxx:detekt` だけでは `app:shared` 等の連鎖的な問題を見落とすため、全体で実行される）
- 全モジュールのコードスタイル検証（`ktlintCheck`）
- モジュールグラフの検証（`assertModuleGraph`）
- モジュール一覧とアーキテクチャドキュメントの一致確認（`assertArchitectureDocModuleList`）
- 全ユニットテスト＋カバレッジレポート生成（`koverXmlReport`）
- Android アプリの lint（`:app:androidApp:lintDebug`）
- Android 実機テスト（androidTest）のコンパイル（`:app:androidApp:compileDebugAndroidTestKotlin`）
- Android アプリ・デスクトップアプリのビルド確認
- デスクトップアプリの統合テスト（Koin モジュール構成の変更は `AppTest` に影響するため）
