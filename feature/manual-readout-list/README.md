# manual-readout-list

手動読み上げタブの listPane を提供する feature モジュール。シミュレーター ID と表示名を受け取り、選択中シミュレーターの保存済みセット一覧を ViewModel で観測する。保存済みセットが0件なら説明シート付きの見出しと、選択中シミュレーター名入りの空状態を表示する。1件以上なら説明見出し行を表示する。右下の「セットを追加」拡張 FAB は常時表示し、UUID を ID とする空のセットを`AddManualReadoutSetUseCase` を通じて保存済みセットの末尾へアトミックに追加する。各セットの行 UI は後続 PR で実装する。説明シートでは読み上げ・順番待ち・データがないときの動作を説明し、デスクトップ版のみホットキーの説明も表示する。アプリの「手動」タブから表示され、シミュレーター切り替え時にペインの状態をリセットする。

## 主要なファイルの役割

- `ManualReadoutListUiState.kt` / `ManualReadoutListViewModel.kt`: 保存済みセット一覧を `StateFlow` で提供し、空のセットを追加・保存する。
- `ManualReadoutListModule.kt`: `named(simulatorId)` の Repository を使う ViewModel の Koin 登録。
- `ManualReadoutListViewModelTest.kt` / `ManualReadoutListModuleTest.kt`: 空一覧・一覧更新・末尾への追加保存・全シミュレーターの観測と保存の Repository 解決を確認する JVM テスト。
- `ManualReadoutListPaneWiringTest.kt`: Pane 経由の状態表示・セット追加の配線とシミュレーター切り替えを確認する JVM テスト。
- `ManualReadoutListPane.kt`: 手動読み上げセット一覧の見出し・説明シート・空状態・セット追加 FAB を表示する UI。
- `IsDesktopPlatform.kt` / `IsDesktopPlatform.android.kt` / `IsDesktopPlatform.nonAndroid.kt`: ホットキー説明の表示に使うプラットフォーム判定。
- `src/commonMain/composeResources/values/strings.xml`: 見出し・説明シート・空状態・セット追加 FAB の表示文言。
- `ManualReadoutListPaneTest.kt`: Content のセット追加 FAB の表示とクリック・見出し・空状態の表示分岐・ヘルプの表示とホットキー説明の表示分岐を確認する JVM テスト。
- `ManualReadoutListPaneScreenshotTest.kt`: 見出し・セット追加 FAB 付きの空状態と説明シート（ホットキーあり・なし）を確認する Desktop/JVM 向けスクリーンショットテスト。
- `ManualReadoutHintSheetContentScreenshotTest.kt`: Android 版の説明シート（ホットキーの説明なし）を確認する Android 向けスクリーンショットテスト。Desktop 版と表示内容が異なるため追加している。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-list.svg)
<!-- MODULE-GRAPH-END -->
