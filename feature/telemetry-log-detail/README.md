# telemetry-log-detail

テレメトリログ詳細を表示する Feature モジュール。

`TelemetryLogDetailFlow` が選択したログIDをもとに `ObserveTelemetryLogDetailUseCase` を購読し、詳細を表示項目へ変換する cold Flow を提供する。`TelemetryLogDetailViewModel` は選択ログIDを保持し、この Flow を `viewModelScope` で `SharingStarted.Eagerly` により StateFlow 化して `uiState` を公開する。`TelemetryLogDetailContent` は `LazyColumn` に選択したログのテレメトリJSONと、一つ前のログがある場合はそのJSONを表示する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-telemetry-log-detail.svg)
<!-- MODULE-GRAPH-END -->
