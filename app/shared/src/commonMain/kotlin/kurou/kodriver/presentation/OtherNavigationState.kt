package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class OtherPaneDestination : NavKey {
    List,
    Detail,
}

/**
 * Other の list/detail ペイン表示状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （ペイン切り替えは push ではなく置き換えとして扱う）。
 */
internal class OtherNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: OtherPaneDestination
        get() = backStack.lastOrNull() as? OtherPaneDestination ?: OtherPaneDestination.List

    fun navigateTo(destination: OtherPaneDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
}

internal val OtherNavigationStateSaver: Saver<OtherNavigationState, String> =
    Saver(
        save = { it.current.name },
        restore = { name ->
            val destination = OtherPaneDestination.entries.find { it.name == name } ?: return@Saver null
            OtherNavigationState(NavBackStack(destination))
        },
    )

@Composable
internal fun rememberOtherNavigationState(
    initial: OtherPaneDestination = OtherPaneDestination.List,
): OtherNavigationState =
    rememberSaveable(saver = OtherNavigationStateSaver) {
        OtherNavigationState(NavBackStack(initial))
    }
