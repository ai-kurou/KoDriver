package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kurou.kodriver.data.dataModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    startKoin {
        modules(listOf(dataModule) + appModules)
    }
    application {
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
}
