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

# 完了報告・PR 作成前の必須チェック一式（全モジュールのdetekt・ktlintCheck・
# assertModuleGraph・assertArchitectureDocModuleList・全ユニットテスト（カバレッジ付き）・
# 両アプリのビルド・デスクトップ統合テスト）
./gradlew preSubmitChecks

# メモリ削減のため org.gradle.workers.max=2・Kotlin in-process・Gradleヒープ2GBに設定しており、その分実行時間は延びる。
# メモリに余裕がある環境では -Dorg.gradle.workers.max=N や ~/.gradle/gradle.properties で上書きできる。

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
