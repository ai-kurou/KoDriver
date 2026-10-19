# manual-readout-list

手動読み上げタブのセット一覧と list/detail ナビゲーションを提供する feature モジュール。選択中シミュレーターの保存済みセットを表示し、追加・読み上げ依頼・削除を行う。詳細画面は `ManualReadoutContent` スロットとして受け取り、このモジュールから詳細モジュールへの依存は持たない。

セットの利用方法、読み上げ仕様、対応項目、保存、詳細編集、ホットキーの全体仕様は [`docs/manual-readout.md`](../../docs/manual-readout.md) を参照する。

## 主要なファイル

- `ManualReadoutContent.kt` / `ManualReadoutNavigationState.kt`: Adaptive scaffold と Navigation 3 による list/detail 切り替え、戻る操作。
- `ManualReadoutListUiState.kt` / `ManualReadoutListViewModel.kt`: セット一覧の購読、空セットの追加、セットIDを指定した読み上げ依頼。
- `ManualReadoutListPane.kt`: 見出し、空状態、一覧行、削除メニューと確認ダイアログ、ヘルプシート、追加FAB。
- `ManualReadoutHotkeyLabel.kt` / `IsManualReadoutHotkeySupported.*.kt`: Windows デスクトップのホットキー表示。
- `ManualReadoutListViewModelTest.kt`, `ManualReadoutListPaneWiringTest.kt`, `ManualReadoutListPaneTest.kt`: ViewModel、配線、画面表示の検証。
- `ManualReadoutListPaneScreenshotTest.kt` / `ManualReadoutHintSheetContentScreenshotTest.kt`: Desktop と Android の画面差分を含むスクリーンショット検証。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-list.svg)
<!-- MODULE-GRAPH-END -->
