package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class ManualReadoutPaneDestination : NavKey {
    List,
    Detail,
}

/**
 * ManualReadout の list/detail ペイン表示状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （ペイン切り替えは push ではなく置き換えとして扱う）。
 */
internal class ManualReadoutNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: ManualReadoutPaneDestination
        get() = backStack.lastOrNull() as? ManualReadoutPaneDestination ?: ManualReadoutPaneDestination.List

    fun navigateTo(destination: ManualReadoutPaneDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
}

@Composable
internal fun rememberManualReadoutNavigationState(
    initial: ManualReadoutPaneDestination = ManualReadoutPaneDestination.List,
): ManualReadoutNavigationState =
    remember {
        ManualReadoutNavigationState(NavBackStack(initial))
    }
