# gt7-ps5-readout-tyre-temperature-detail

GT7 PS5 のタイヤ温度アナウンス詳細設定を提供する feature モジュールです。
読み上げ一覧の「タイヤ温度」項目から表示されます。

## Responsibilities

- `Gt7Ps5ReadoutTyreTemperatureDetailPane` でタイトル・説明を表示する
- 現状は静的な説明表示のみで、閾値設定などのユースケース連携は未実装（今後追加予定）

## Related Modules

- `:core:designsystem`: 共通 Composable コンポーネント（`DetailPaneDescription` / `DetailPaneCard`）
- `:app:shared`: 読み上げ一覧から detail pane へ遷移

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-gt7-ps5-readout-tyre-temperature-detail.svg)
<!-- MODULE-GRAPH-END -->
