# telemetry-log-detail

テレメトリログ詳細を表示する Feature モジュール。

`TelemetryLogDetailViewModel` が選択したログIDをもとに `ObserveTelemetryLogDetailUseCase` を購読し、`uiState` を公開する。`TelemetryLogDetailContent` は `LazyColumn` に選択したログのテレメトリJSONと、一つ前のログがある場合はそのJSONを表示する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-telemetry-log-detail.svg)
<!-- MODULE-GRAPH-END -->
