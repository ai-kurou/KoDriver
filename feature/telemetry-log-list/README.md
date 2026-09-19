# telemetry-log-list

記録済みテレメトリログの一覧画面を提供する feature モジュール。

## 主要なファイルの役割

- `TelemetryLogListPane.kt` / `TelemetryLogContent.kt`: 一覧画面のUI。`TelemetryLogContent.kt` は `ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）と同様に `Material3 Adaptive` の `ListDetailPaneScaffold` と `TelemetryLogNavigationState`（Navigation 3の `NavBackStack`）を組み合わせるパターンを使う（詳細は `docs/list-detail-navigation-pattern.md` を参照）。
- `TelemetryLogListViewModel.kt`: ログ一覧・選択中ログ・削除確認ダイアログ表示状態などを `StateFlow` で公開する。
- `ObserveSortedTelemetryLogsUseCase.kt`: `:core:domain` の `ObserveTelemetryLogsUseCase` を作成日時の降順にソートして提供する、この feature 固有のUseCase。
- `TelemetryLogListModule.kt`: この feature の Koin モジュール定義。
- `TelemetryLogDeleteConfirmDialog.kt` / `TelemetryLogResetConfirmDialog.kt`: 個別削除・全件リセット時の確認ダイアログ。
- `ReadoutItemDisplayName.kt`: ログに紐づく `ReadoutItemKey` の表示名変換。
- `ErrorCapture.kt`: プラットフォームごとのエラーレポート送信（expect/actual）。

## 読み上げ結果（NarrationOutcome）の表示

一覧の各行は、`:core:domain` の `NarrationOutcome` に応じて末尾のアイコン・アクセシビリティ用の説明・前景色を切り替える。

| `NarrationOutcome` | アイコン | contentDescription | 前景色 |
| --- | --- | --- | --- |
| `QUEUED` | `Icons.AutoMirrored.Filled.PlaylistAdd` | キューに追加 | 通常 |
| `INTERRUPTED` | `Icons.Filled.PlaylistRemove` | 割り込み再生 | 通常 |
| `SKIPPED` | `Icons.Filled.VolumeOff` | 読み上げなし | `SKIPPED_CONTENT_ALPHA`（0.38）で減光 |

`SKIPPED` は「読み上げ条件は整ったが、キュー再生が無効で優先度に負けたため読み上げされなかった」ことを表す。実際には音が鳴っていないため、`feature:readout-list` の OFF 項目と同じ減光表現（アルファ 0.38）を使い、読み上げ済みの行と区別する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-telemetry-log-list.svg)
<!-- MODULE-GRAPH-END -->
