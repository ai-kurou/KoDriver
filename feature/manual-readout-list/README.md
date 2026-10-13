# manual-readout-list

手動読み上げタブの listPane を提供する feature モジュール。現状はモジュールの土台のみで、セットがまだない空状態を表示する。アプリのタブへの配線は後続 PR で行う。

## 主要なファイルの役割

- `ManualReadoutListPane.kt`: 手動読み上げセット一覧の空状態を表示する UI。
- `src/commonMain/composeResources/values/strings.xml`: 空状態の表示文言。
- `ManualReadoutListPaneTest.kt`: 空状態の表示文言を確認する JVM テスト。
- `ManualReadoutListPaneScreenshotTest.kt`: 空状態を確認する Desktop/JVM 向けスクリーンショットテスト。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-list.svg)
<!-- MODULE-GRAPH-END -->
