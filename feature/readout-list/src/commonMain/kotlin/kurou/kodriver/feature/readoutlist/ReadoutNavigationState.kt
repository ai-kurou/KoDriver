package kurou.kodriver.feature.readoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class ReadoutPaneDestination : NavKey {
    List,
    Detail,
}

/**
 * Readout の list/detail ペイン表示状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （ペイン切り替えは push ではなく置き換えとして扱う）。
 */
internal class ReadoutNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: ReadoutPaneDestination
        get() = backStack.lastOrNull() as? ReadoutPaneDestination ?: ReadoutPaneDestination.List

    fun navigateTo(destination: ReadoutPaneDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
}

internal val ReadoutNavigationStateSaver: Saver<ReadoutNavigationState, String> =
    Saver(
        save = { it.current.name },
        restore = { name ->
            val destination = ReadoutPaneDestination.entries.find { it.name == name } ?: return@Saver null
            ReadoutNavigationState(NavBackStack(destination))
        },
    )

@Composable
internal fun rememberReadoutNavigationState(
    initial: ReadoutPaneDestination = ReadoutPaneDestination.List,
): ReadoutNavigationState =
    rememberSaveable(saver = ReadoutNavigationStateSaver) {
        ReadoutNavigationState(NavBackStack(initial))
    }
