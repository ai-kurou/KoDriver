# other-license-detail

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-license-detail.svg)
<!-- MODULE-GRAPH-END -->

## ライセンス一覧

`aboutlibraries.json` は Gradle 依存から自動生成するが、Vosk 本体と同梱の日本語モデル（`vosk-model-small-ja-0.22`）は
このモジュールの依存ではないため、`config/libraries/*.json` で手動登録している（`aboutLibraries.collect.configPath`）。
Vosk のバージョンやモデルを変更した場合は、このJSONも更新する。
