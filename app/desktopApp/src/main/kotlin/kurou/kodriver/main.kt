package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.sentry.Sentry
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.lmuwindowsdata.lmuWindowsDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.Dimension

private const val SENTRY_DSN =
    "https://93dc09daf8552c39b0eea61b4f1319ee@o4511575800676352.ingest.us.sentry.io/4511575816667136"

fun main() {
    Sentry.init { options ->
        options.dsn = SENTRY_DSN
    }
    val koinApplication = startKoin {
        modules(
            listOf(desktopDataModule, lmuWindowsDataModule, gt7Ps5DataModule) +
                appModules +
                listOf(module { single(named("appVersion")) { APP_VERSION } }),
        )
    }
    val server = createKoDriverServer(koinApplication.koin)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })
    try {
        application {
            var exitRequested by remember { mutableStateOf(false) }
            val windowState = rememberWindowState(size = DpSize(800.dp, 500.dp))
            Window(
                onCloseRequest = { exitRequested = true },
                title = "KoDriver",
                state = windowState,
                icon = painterResource("launcher.png"),
            ) {
                SideEffect { window.minimumSize = Dimension(600, 500) }
                AppScreen(
                    exitRequested = exitRequested,
                    onExitRequestConsumed = { exitRequested = false },
                    onExit = ::exitApplication,
                )
            }
        }
    } finally {
        server.stop()
    }
}
