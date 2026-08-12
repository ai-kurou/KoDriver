package kurou.kodriver.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class AppDestination(
    val icon: ImageVector,
) : NavKey {
    Readout(Icons.Default.HeadsetMic),
    Log(Icons.Default.Description),
    More(Icons.Default.MoreHoriz),
}

/**
 * トップレベルのタブ切り替え状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （タブ切り替えは push ではなく置き換えとして扱う）。
 */
internal class AppNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: AppDestination
        get() = backStack.lastOrNull() as? AppDestination ?: AppDestination.Readout

    fun navigateTo(destination: AppDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }

    fun handleTabClick(
        destination: AppDestination,
        onReselected: (AppDestination) -> Unit,
    ) {
        if (current == destination) {
            onReselected(destination)
        } else {
            navigateTo(destination)
        }
    }
}

internal val AppNavigationStateSaver: Saver<AppNavigationState, String> =
    Saver(
        save = { it.current.name },
        restore = { name ->
            val destination = AppDestination.entries.find { it.name == name } ?: return@Saver null
            AppNavigationState(NavBackStack(destination))
        },
    )

@Composable
internal fun rememberAppNavigationState(initial: AppDestination = AppDestination.Readout): AppNavigationState =
    rememberSaveable(saver = AppNavigationStateSaver) { AppNavigationState(NavBackStack(initial)) }
