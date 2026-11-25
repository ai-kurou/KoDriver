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
 * デスクトップアプリの起動時に、スプラッシュ画面を表示しながら初期化を実行するホスト。
 *
 * [initializeModules] と [startServer] を順に実行し、その進捗を [DesktopSplashScreen] に
 * 表示する。初期化が完了し、スプラッシュ画面の完了演出（光が横切る）が終わると [onFinished] を
 * 一度だけ通知する。スプラッシュ画面は表示し続けるため、呼び出し側が通知を受けて
 * スプラッシュのウィンドウを閉じ、メインウィンドウを開く。
 *
 * 初期化処理が例外をスローした場合はスプラッシュ画面が完了しないまま停止するため、
 * エラーダイアログを表示する。ダイアログを閉じると [onError] を通知し、アプリ終了などの
 * 後処理を呼び出し側に委ねる。
 *
 * @param appVersion スプラッシュ画面に表示するアプリのバージョン。
 * @param initializeModules Koin モジュール構築など、依存グラフの初期化処理。
 * @param startServer Ktor サーバーの起動処理。
 * @param onFinished 初期化と完了演出が終わったときに一度だけ呼ばれるコールバック。
 * @param onError 初期化失敗時、エラーダイアログを閉じたときに呼ばれるコールバック。
 */
@Composable
public fun DesktopSplashHost(
    appVersion: String,
    initializeModules: suspend () -> Unit,
    startServer: suspend () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
) {
    val progress = remember { DesktopSplashProgress() }
    val uiState by progress.uiState.collectAsState()
    var error by remember { mutableStateOf<Throwable?>(null) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        try {
            progress.runInitialization(
                initializeModules = initializeModules,
                startServer = startServer,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            error = e
        }
    }

    DesktopSplashScreen(
        uiState = uiState,
        appVersion = appVersion,
        modifier = modifier,
        onReadyAnimationFinished = { currentOnFinished() },
    )

    error?.let { throwable ->
        DesktopSplashErrorDialog(
            message = throwable.message ?: "不明なエラーが発生しました。",
            onConfirm = { onError(throwable) },
        )
    }
}
