package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.desktopsplash.DesktopSplashProgress
import kurou.kodriver.feature.desktopsplash.DesktopSplashScreen
import kurou.kodriver.feature.desktopsplash.runInitialization

/**
 * デスクトップアプリの起動時に、初期化が完了するまでスプラッシュ画面を表示するホスト。
 *
 * [initializeModules] と [startServer] を順に実行し、その進捗を [DesktopSplashScreen] に
 * 表示する。初期化が完了したら [content]（メイン画面）へ切り替え、[onReady] を通知する。
 *
 * @param initializeModules Koin モジュール構築など、依存グラフの初期化処理。
 * @param startServer Ktor サーバーの起動処理。
 * @param onReady 初期化完了時に一度だけ呼ばれるコールバック。
 * @param content 初期化完了後に表示するメイン画面。
 */
@Composable
fun DesktopSplashHost(
    initializeModules: suspend () -> Unit,
    startServer: suspend () -> Unit,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val progress = remember { DesktopSplashProgress() }
    val uiState by progress.uiState.collectAsState()

    LaunchedEffect(Unit) {
        progress.runInitialization(
            initializeModules = initializeModules,
            startServer = startServer,
        )
        onReady()
    }

    if (uiState.isReady) {
        content()
    } else {
        DesktopSplashScreen(uiState = uiState, modifier = modifier)
    }
}
