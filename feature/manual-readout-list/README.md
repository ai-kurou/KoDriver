# manual-readout-list

手動読み上げタブの listPane を提供する feature モジュール。現状はモジュールの土台のみで、説明シート付きの見出しと、セットがまだない空状態を表示する。説明シートでは読み上げ・順番待ち・データがないときの動作を説明し、デスクトップ版のみホットキーの説明も表示する。アプリのタブへの配線は後続 PR で行う。

## 主要なファイルの役割

- `ManualReadoutListPane.kt`: 手動読み上げセット一覧の見出し・説明シート・空状態を表示する UI。
- `IsDesktopPlatform.kt` / `IsDesktopPlatform.android.kt` / `IsDesktopPlatform.nonAndroid.kt`: ホットキー説明の表示に使うプラットフォーム判定。
- `src/commonMain/composeResources/values/strings.xml`: 見出し・説明シート・空状態の表示文言。
- `ManualReadoutListPaneTest.kt`: 見出し・空状態・ヘルプの表示とホットキー説明の表示分岐を確認する JVM テスト。
- `ManualReadoutListPaneScreenshotTest.kt`: 見出し付きの空状態と説明シート（ホットキーあり・なし）を確認する Desktop/JVM 向けスクリーンショットテスト。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-list.svg)
<!-- MODULE-GRAPH-END -->
