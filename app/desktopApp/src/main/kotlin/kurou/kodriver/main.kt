package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kurou.kodriver.presentation.AppScreen
import java.awt.Dimension

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(1200.dp, 700.dp))
    Window(
        onCloseRequest = ::exitApplication,
        title = "KoDriver",
        state = windowState,
    ) {
        SideEffect { window.minimumSize = Dimension(900, 600) }
        AppScreen()
    }
}
