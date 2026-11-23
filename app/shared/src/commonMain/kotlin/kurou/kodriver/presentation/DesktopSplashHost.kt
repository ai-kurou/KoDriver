package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kurou.kodriver.feature.splash.desktop.DesktopSplashErrorDialog
import kurou.kodriver.feature.splash.desktop.DesktopSplashProgress
import kurou.kodriver.feature.splash.desktop.DesktopSplashScreen
import kurou.kodriver.feature.splash.desktop.runInitialization

/**
 * デスクトップアプリの起動時に、初期化が完了するまでスプラッシュ画面を表示するホスト。
 *
 * [initializeModules] と [startServer] を順に実行し、その進捗を [DesktopSplashScreen] に
 * 表示する。初期化が完了すると、スプラッシュ画面の完了演出（光が横切る）が終わるのを待って
 * [content]（メイン画面）へ切り替える。[onReady] は初期化の完了時に通知する。
 *
 * 初期化処理が例外をスローした場合はスプラッシュ画面が完了しないまま停止するため、
 * エラーダイアログを表示する。ダイアログを閉じると [onError] を通知し、アプリ終了などの
 * 後処理を呼び出し側に委ねる。
 *
 * @param initializeModules Koin モジュール構築など、依存グラフの初期化処理。
 * @param startServer Ktor サーバーの起動処理。
 * @param onReady 初期化完了時に一度だけ呼ばれるコールバック。
 * @param onError 初期化失敗時、エラーダイアログを閉じたときに呼ばれるコールバック。
 * @param content 初期化完了後に表示するメイン画面。
 */
@Composable
public fun DesktopSplashHost(
    initializeModules: suspend () -> Unit,
    startServer: suspend () -> Unit,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val progress = remember { DesktopSplashProgress() }
    val uiState by progress.uiState.collectAsState()
    var error by remember { mutableStateOf<Throwable?>(null) }
    var readyAnimationFinished by remember { mutableStateOf(false) }
    val currentOnReady by rememberUpdatedState(onReady)

    LaunchedEffect(Unit) {
        try {
            progress.runInitialization(
                initializeModules = initializeModules,
                startServer = startServer,
            )
            currentOnReady()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            error = e
        }
    }

    if (uiState.isReady && readyAnimationFinished) {
        content()
    } else {
        DesktopSplashScreen(
            uiState = uiState,
            modifier = modifier,
            onReadyAnimationFinished = { readyAnimationFinished = true },
        )
    }

    error?.let { throwable ->
        DesktopSplashErrorDialog(
            message = throwable.message ?: "不明なエラーが発生しました。",
            onConfirm = { onError(throwable) },
        )
    }
}
