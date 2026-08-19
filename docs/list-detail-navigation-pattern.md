# list/detail ペイン切り替えの Navigation 3 パターン

`ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）は、`Material3 Adaptive` の `ListDetailPaneScaffold` と並行して、Navigation 3 の `NavBackStack<NavKey>` を「現在どちらのペインを表示しているか」の状態として保持する共通パターンを使う。新しく list/detail 構成の画面を追加する場合は、これらの実装（`XxxNavigationState.kt` とその `XxxNavigationStateTest.kt`）を参照すること。

- `internal enum class XxxPaneDestination : NavKey { List, Detail }` でペインの宛先を定義する。
- `internal class XxxNavigationState(val backStack: NavBackStack<NavKey>)` が `current`（`backStack.lastOrNull() as? XxxPaneDestination ?: XxxPaneDestination.List`）と `navigateTo(destination)`（現在と異なる場合のみ `clear()` してから `add()` する置き換え）を提供する。
- `@Composable internal fun rememberXxxNavigationState(initial: XxxPaneDestination = XxxPaneDestination.List)` は `remember { XxxNavigationState(NavBackStack(initial)) }` で生成する。**`rememberSaveable` は使わない**（後述）。
- 呼び出し側（`XxxContent.kt`）では `navigationState` の `initial` を ViewModel の `uiState.selectedXxx` から導出し、`LaunchedEffect(uiState.selectedXxx)` で `uiState.selectedXxx` の変化のたびに `navigationState.navigateTo(...)` を呼んで同期したうえで、その結果（`navigationState.current`）を使って `ListDetailPaneScaffoldRole` へ `navigator.navigateTo(...)` する。詳細ペインの実際の表示内容は `navigationState.current` ではなく `uiState.selectedXxx` を直接 `let` で分岐して描画する。

## `rememberSaveable`/`Saver` を使わない理由

選択状態の唯一の正（`_selectedItem` 等）は各 ViewModel 内のプレーンな `MutableStateFlow` であり、`SavedStateHandle` や DataStore に永続化されていない。そのため `LaunchedEffect(uiState.selectedXxx)` は初回コンポジションでも必ず一度発火し、`uiState.selectedXxx`（プロセス再生成後は常に未選択）に基づいて `navigationState` を無条件に上書きする。`rememberSaveable` で `navigationState` 側だけを復元しても、直後にこの同期で上書きされるため実質的に意味を持たない。選択状態自体の永続化が必要になった場合は、まず ViewModel 側（`SavedStateHandle` 等）で対応すること。
