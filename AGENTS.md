# AGENTS.md

このプロジェクトのアーキテクチャ、コーディング規約、ワークフロー、およびその他の重要な指示については、**`CLAUDE.md`** を参照し、そこに記載されているすべてのルールをプロジェクト指示として遵守してください。

## Gradle 実行時の Java

`./gradlew preSubmitChecks` などの Gradle コマンドを実行する前に、`./gradlew --version` を実行して `Daemon JVM` を確認してください。このリポジトリでは `gradle/gradle-daemon-jvm.properties` の `toolchainVersion` と `toolchainVendor` が Gradle デーモン JVM を指定します。必要な JVM が見つからない場合は、同ファイルの設定に従って Gradle が解決・ダウンロードする仕組み（Foojay resolver と `~/.gradle/jdks/`）を確認し、JDK 21 の導入または解決を行ってください。

Android Studio 同梱 JBR の Java 実行ファイル `/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java` は、Gradle ランチャー側の Java が必要な場合に使用できます。ただし、このパスを使うだけでは Gradle デーモンの JVM は切り替わりません。デーモン JVM の指定を変更する必要がある場合は、まず `gradle/gradle-daemon-jvm.properties` を確認してください。個人環境固有の `JAVA_HOME` や JDK の絶対パスをプロジェクト設定へ固定しないでください。

## Code Review Rules

- PRレビューの指摘タイトル、説明、レビュー要約は日本語で記述してください。
- コード、識別子、ファイル名、エラーメッセージは原文を維持してください。
