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
import io.sentry.protocol.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.core.acewindowsdata.aceWindowsDataModule
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.lmuwindowsdata.lmuWindowsDataModule
import kurou.kodriver.data.AnonymousUserId
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.DesktopSplashHost
import kurou.kodriver.presentation.featureModules
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.Dimension

private const val SENTRY_DSN =
    "https://93dc09daf8552c39b0eea61b4f1319ee@o4511575800676352.ingest.us.sentry.io/4511575816667136"
private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"

/**
 * アプリケーションを起動するエントリーポイント。
 */
fun main() {
    Sentry.init { options ->
        options.dsn = SENTRY_DSN
        options.environment = "desktop"
    }
    Sentry.setUser(
        User().apply { id = AnonymousUserId.getOrCreate(kodriverDirectory) },
    )

    var koin: Koin? = null
    var server: KoDriverServer? = null
    try {
        application {
            var exitRequested by remember { mutableStateOf(false) }
            var ready by remember { mutableStateOf(false) }
            val windowState = rememberWindowState(size = DpSize(800.dp, 500.dp))
            Window(
                onCloseRequest = {
                    if (shouldConfirmExit(ready = ready, shuttingDown = SystemShutdownState.isShuttingDown)) {
                        exitRequested = true
                    } else {
                        exitApplication()
                    }
                },
                title = "KoDriver",
                state = windowState,
                icon = painterResource("launcher.png"),
            ) {
                SideEffect { window.minimumSize = Dimension(600, 500) }
                DesktopSplashHost(
                    initializeModules = {
                        withContext(Dispatchers.Default) {
                            koin =
                                startKoin {
                                    // composition root: データ層モジュール（:core:*data）＋ 全 feature の Koin モジュール
                                    // （featureModules）＋ アプリバージョン定数（named("appVersion")。server-connection 等が
                                    // get で解決）を束ねる。
                                    modules(
                                        listOf(
                                            desktopDataModule,
                                            lmuWindowsDataModule,
                                            gt7Ps5DataModule,
                                            aceWindowsDataModule,
                                        ) +
                                            featureModules +
                                            listOf(module { single(named("appVersion")) { APP_VERSION } }),
                                    )
                                }.koin
                        }
                    },
                    startServer = {
                        withContext(Dispatchers.IO) {
                            val startedServer = createKoDriverServer(requireNotNull(koin)).also { it.start() }
                            server = startedServer
                            // OS のシャットダウン・ログオフでも呼ばれる。終了確認ダイアログでユーザー操作を
                            // 待たないようフラグを立ててから、サーバーを停止する。
                            // UI の終了は Compose の ApplicationScope 側（onCloseRequest）に任せる。
                            // ここで exitApplication() を呼ぶとシャットダウン中の JVM から EDT へ
                            // コンポジション破棄を投げることになり、フックが長引く恐れがある。
                            Runtime.getRuntime().addShutdownHook(
                                Thread {
                                    SystemShutdownState.markShuttingDown()
                                    startedServer.stop()
                                },
                            )
                        }
                    },
                    onReady = { ready = true },
                    onError = { throwable ->
                        Sentry.captureException(throwable)
                        exitApplication()
                    },
                ) {
                    AppScreen(
                        exitRequested = exitRequested,
                        onExitRequestConsumed = { exitRequested = false },
                        onExit = ::exitApplication,
                    )
                }
            }
        }
    } finally {
        server?.stop()
    }
}
