package kurou.kodriver.feature.splash.desktop

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.splash.generated.resources.Res
import kurou.kodriver.feature.splash.generated.resources.desktop_splash_error_close
import kurou.kodriver.feature.splash.generated.resources.desktop_splash_error_title
import kurou.kodriver.feature.splash.generated.resources.desktop_splash_server_error_message
import kurou.kodriver.feature.splash.generated.resources.desktop_splash_server_error_title
import org.jetbrains.compose.resources.stringResource

/**
 * 起動時の初期化に失敗したことを通知するダイアログ。
 *
 * スプラッシュ画面が完了しないまま停止するのを避けるため、初期化処理で例外が
 * 発生した場合にエラー内容を表示する。閉じるボタンの押下で [onConfirm] を呼び、
 * 呼び出し側（アプリ終了など）に後処理を委ねる。
 *
 * @param message 表示するエラーメッセージ。
 * @param onConfirm 閉じるボタン押下時に呼ばれるコールバック。
 */
@Composable
public fun DesktopSplashErrorDialog(
    message: String,
    onConfirm: () -> Unit,
) {
    SplashErrorAlertDialog(
        title = stringResource(Res.string.desktop_splash_error_title),
        message = message,
        onConfirm = onConfirm,
    )
}

/**
 * サーバーの起動に失敗したことを通知するダイアログ。
 *
 * 起動は継続できるため、閉じるボタンの押下で [onConfirm] を呼び、呼び出し側に起動の続行を委ねる。
 *
 * @param onConfirm 閉じるボタン押下時に呼ばれるコールバック。
 */
@Composable
public fun DesktopSplashServerErrorDialog(onConfirm: () -> Unit) {
    SplashErrorAlertDialog(
        title = stringResource(Res.string.desktop_splash_server_error_title),
        message = stringResource(Res.string.desktop_splash_server_error_message),
        onConfirm = onConfirm,
    )
}

@Composable
private fun SplashErrorAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = { Text(title) },
        // スプラッシュの小さいウィンドウ内に表示するため、長いエラー文はスクロールできるようにする。
        text = { Text(message, modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.desktop_splash_error_close))
            }
        },
    )
}
