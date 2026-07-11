package kurou.kodriver.feature.desktopsplash

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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
        title = { Text("起動に失敗しました") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("閉じる")
            }
        },
    )
}
