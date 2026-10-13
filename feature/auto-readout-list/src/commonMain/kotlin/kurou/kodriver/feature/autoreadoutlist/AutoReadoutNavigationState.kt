package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class AutoReadoutPaneDestination : NavKey {
    List,
    Detail,
}

/**
 * Readout の list/detail ペイン表示状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （ペイン切り替えは push ではなく置き換えとして扱う）。
 */
internal class AutoReadoutNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: AutoReadoutPaneDestination
        get() = backStack.lastOrNull() as? AutoReadoutPaneDestination ?: AutoReadoutPaneDestination.List

    fun navigateTo(destination: AutoReadoutPaneDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
}

@Composable
internal fun rememberAutoReadoutNavigationState(
    initial: AutoReadoutPaneDestination = AutoReadoutPaneDestination.List,
): AutoReadoutNavigationState =
    remember {
        AutoReadoutNavigationState(NavBackStack(initial))
    }
