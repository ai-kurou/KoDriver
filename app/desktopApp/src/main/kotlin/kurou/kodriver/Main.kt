package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.sentry.Sentry
import io.sentry.protocol.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.core.acewindowsdata.aceWindowsDataModule
import kurou.kodriver.core.acewindowsdata.desktopAceWindowsPreferencesDataModule
import kurou.kodriver.core.devicevolumedata.deviceVolumeDataModule
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.lmuwindowsdata.desktopLmuWindowsPreferencesDataModule
import kurou.kodriver.core.lmuwindowsdata.lmuWindowsDataModule
import kurou.kodriver.core.lmuwindowsrestapidata.lmuWindowsRestApiDataModule
import kurou.kodriver.core.serverdiscoverydata.serverDiscoveryDataModule
import kurou.kodriver.core.speechrecognitiondata.speechRecognitionDataModule
import kurou.kodriver.core.texttospeechdata.textToSpeechDataModule
import kurou.kodriver.core.windowshotkeydata.windowsHotkeyDataModule
import kurou.kodriver.core.windowsstartupdata.windowsStartupDataModule
import kurou.kodriver.data.AnonymousUserId
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.data.registerReadoutItemKeys
import kurou.kodriver.generated.resources.Res
import kurou.kodriver.generated.resources.launcher
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.DesktopSplashHost
import kurou.kodriver.presentation.featureModules
import kurou.kodriver.presentation.rememberMainWindowBounds
import kurou.kodriver.presentation.rememberMainWindowBoundsSaver
import org.jetbrains.compose.resources.painterResource
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.Dimension

private const val SENTRY_DSN =
    "https://93dc09daf8552c39b0eea61b4f1319ee@o4511575800676352.ingest.us.sentry.io/4511575816667136"

/** スプラッシュウィンドウの大きさ（16:9）。右側に置く正方形の背景写真が崩れない横長にする。 */
private val SPLASH_WINDOW_SIZE = DpSize(720.dp, 405.dp)
private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"

/**
 * アプリケーションを起動するエントリーポイント。
 */
@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    registerReadoutItemKeys()
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
            val windowState = rememberWindowState(size = DpSize(1000.dp, 600.dp))
            var koinReady by remember { mutableStateOf(false) }
            var splashFinished by remember { mutableStateOf(false) }
            var boundsRestored by remember { mutableStateOf(false) }
            // Koin はウィンドウより後に初期化されるため、位置・サイズの復元と保存は準備完了後に始める。
            // スプラッシュの表示中に復元を済ませ、メインウィンドウは最初から保存した位置・サイズで開く。
            if (koinReady) {
                MainWindowBoundsPersistence(
                    windowState = windowState,
                    savedBounds = rememberMainWindowBounds(),
                    onBoundsChange = rememberMainWindowBoundsSaver(),
                    onRestored = { boundsRestored = true },
                )
            }
            if (splashFinished && boundsRestored) {
                Window(
                    onCloseRequest = { exitApplication() },
                    title = "KoDriver",
                    state = windowState,
                    icon = painterResource(Res.drawable.launcher),
                ) {
                    SideEffect { window.minimumSize = Dimension(600, 500) }
                    AppScreen()
                }
                // Narrator Overlay はデスクトップアプリのみの機能（Android版には存在しない）。メインウィンドウと同じ
                // application スコープ内で開くため、exitApplication() 時にこのウィンドウも一緒に閉じる。
                // 表示ON/OFFはその他タブの「オーバーレイ設定」で切り替えられる。
                NarratorOverlayWindowHost()
            } else {
                // 起動中は、枠なし・角丸のスプラッシュ専用ウィンドウだけを画面中央に表示する。
                // 角の外側と背景を透けさせるため、ウィンドウを透過にする（透過には枠なしが必須）。
                Window(
                    onCloseRequest = { exitApplication() },
                    title = "KoDriver",
                    state =
                        rememberWindowState(
                            size = SPLASH_WINDOW_SIZE,
                            position = WindowPosition.Aligned(Alignment.Center),
                        ),
                    icon = painterResource(Res.drawable.launcher),
                    decoration = WindowDecoration.Undecorated(),
                    transparent = true,
                    resizable = false,
                ) {
                    DesktopSplashHost(
                        appVersion = APP_VERSION,
                        initializeModules = {
                            withContext(Dispatchers.Default) {
                                koin =
                                    startKoin {
                                        // composition root: データ層モジュール（:core:*data）＋ 全 feature の Koin モジュール
                                        // （featureModules）＋ アプリバージョン定数（named("appVersion")。server-connection 等が
                                        // get で解決）を束ねる。
                                        modules(
                                            listOf(
                                                desktopDataModule(kodriverDirectory),
                                                desktopAceWindowsPreferencesDataModule(kodriverDirectory),
                                                desktopLmuWindowsPreferencesDataModule(kodriverDirectory),
                                                lmuWindowsDataModule,
                                                lmuWindowsRestApiDataModule,
                                                gt7Ps5DataModule,
                                                aceWindowsDataModule,
                                                deviceVolumeDataModule,
                                                serverDiscoveryDataModule,
                                                speechRecognitionDataModule,
                                                textToSpeechDataModule,
                                                windowsStartupDataModule,
                                                windowsHotkeyDataModule,
                                            ) +
                                                featureModules +
                                                listOf(module { single(named("appVersion")) { APP_VERSION } }),
                                        )
                                    }.koin
                                koinReady = true
                            }
                        },
                        startServer = {
                            withContext(Dispatchers.IO) {
                                val startedServer = createKoDriverServer(requireNotNull(koin)).also { it.start() }
                                server = startedServer
                                Runtime.getRuntime().addShutdownHook(Thread { startedServer.stop() })
                            }
                        },
                        onFinished = { splashFinished = true },
                        onError = { throwable ->
                            Sentry.captureException(throwable)
                            exitApplication()
                        },
                    )
                }
            }
        }
    } finally {
        server?.stop()
    }
}
