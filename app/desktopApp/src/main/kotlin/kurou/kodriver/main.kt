package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.sentry.Sentry
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin
import java.awt.Dimension

private const val SENTRY_DSN =
    "https://93dc09daf8552c39b0eea61b4f1319ee@o4511575800676352.ingest.us.sentry.io/4511575816667136"

fun main() {
    Sentry.init { options ->
        options.dsn = SENTRY_DSN
    }
    val koinApplication = startKoin {
        modules(listOf(desktopDataModule) + appModules)
    }
    val server = createKoDriverServer(koinApplication.koin)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
    try {
        application {
            val windowState = rememberWindowState(size = DpSize(800.dp, 500.dp))
            Window(
                onCloseRequest = ::exitApplication,
                title = "KoDriver",
                state = windowState,
            ) {
                SideEffect { window.minimumSize = Dimension(600, 500) }
                AppScreen()
            }
        }
    } finally {
        server.stop()
    }
}
