package kurou.kodriver.feature.telemetryloglist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal enum class TelemetryLogPaneDestination : NavKey {
    List,
    Detail,
}

/**
 * TelemetryLog の list/detail ペイン表示状態を保持するクラス。
 * Navigation 3 の NavBackStack を、単一要素のバックスタックとして利用する
 * （ペイン切り替えは push ではなく置き換えとして扱う）。
 */
internal class TelemetryLogNavigationState(
    val backStack: NavBackStack<NavKey>,
) {
    val current: TelemetryLogPaneDestination
        get() = backStack.lastOrNull() as? TelemetryLogPaneDestination ?: TelemetryLogPaneDestination.List

    fun navigateTo(destination: TelemetryLogPaneDestination) {
        if (current != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
}

@Composable
internal fun rememberTelemetryLogNavigationState(
    initial: TelemetryLogPaneDestination = TelemetryLogPaneDestination.List,
): TelemetryLogNavigationState =
    remember {
        TelemetryLogNavigationState(NavBackStack(initial))
    }
