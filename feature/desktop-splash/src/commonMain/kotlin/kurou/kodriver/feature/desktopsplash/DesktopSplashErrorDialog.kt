package kurou.kodriver.feature.desktopsplash

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import kurou.kodriver.feature.desktopsplash.generated.resources.Res
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_error_close
import kurou.kodriver.feature.desktopsplash.generated.resources.desktop_splash_error_title
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
fun DesktopSplashErrorDialog(
    message: String,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = { Text(stringResource(Res.string.desktop_splash_error_title)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.desktop_splash_error_close))
            }
        },
    )
}
