package kurou.kodriver.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class AppDestination(
    val icon: ImageVector,
) {
    Readout(Icons.Default.HeadsetMic),
    Log(Icons.Default.Description),
    More(Icons.Default.MoreHoriz),
}

/**
 * トップレベルのタブ切り替え状態を保持するクラス。
 */
internal class AppNavigationState(
    private val destinationState: MutableState<AppDestination>,
) {
    val current: AppDestination
        get() = destinationState.value

    fun navigateTo(destination: AppDestination) {
        destinationState.value = destination
    }

    fun handleTabClick(
        destination: AppDestination,
        onReselected: (AppDestination) -> Unit,
    ) {
        if (destinationState.value == destination) {
            onReselected(destination)
        }
        destinationState.value = destination
    }
}

@Composable
internal fun rememberAppNavigationState(initial: AppDestination = AppDestination.Readout): AppNavigationState {
    val destinationState = rememberSaveable { mutableStateOf(initial) }
    return remember(destinationState) { AppNavigationState(destinationState) }
}
