package kurou.kodriver.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import kurou.kodriver.feature.otherlist.rememberOpenAccessLocalNetworkPermissionSettings

/**
 * rememberAccessLocalNetworkPermissionRequester のこのプラットフォーム向け実装。
 * 通常は権限のダイアログを出す。恒久的に拒否されたと判断できる場合は、次回から設定画面を開く。
 *
 * ACCESS_LOCAL_NETWORK は API 37 で追加された定数だが、コンパイル時に文字列へインライン展開されるため
 * 古い端末で参照しても実行時エラーにならない。権限が必要な端末（未許可時のみバナーや画面を開いたときから呼ばれる）でしか使われない。
 */
@SuppressLint("InlinedApi")
@Composable
actual fun rememberAccessLocalNetworkPermissionRequester(
    onResult: () -> Unit,
): (openSettingsWhenPermanentlyDenied: Boolean) -> Unit {
    val activity = LocalContext.current.findActivity()
    val currentOnResult by rememberUpdatedState(onResult)
    val openSettings = rememberOpenAccessLocalNetworkPermissionSettings()
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var ambiguousDenials by rememberSaveable { mutableIntStateOf(0) }
    var rationaleBeforeLaunch by remember { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val rationaleAfter = activity.shouldShowRationale()
            when {
                granted || rationaleAfter -> {
                    ambiguousDenials = 0
                }

                // 要求前は根拠の表示対象だったのに、拒否後は対象でなくなった。「今後表示しない」が選ばれた
                rationaleBeforeLaunch -> {
                    permanentlyDenied = true
                }

                // 初回のダイアログを閉じただけの場合も同じ状態になるため、2回続いたときに初めて恒久拒否とみなす
                else -> {
                    ambiguousDenials++
                    permanentlyDenied = ambiguousDenials >= AMBIGUOUS_DENIALS_FOR_PERMANENT_DENIAL
                }
            }
            currentOnResult()
        }
    return remember(launcher, openSettings, activity) {
        val request: (Boolean) -> Unit = { openSettingsWhenPermanentlyDenied ->
            if (permanentlyDenied) {
                if (openSettingsWhenPermanentlyDenied) openSettings()
            } else {
                rationaleBeforeLaunch = activity.shouldShowRationale()
                launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }
        request
    }
}

private const val AMBIGUOUS_DENIALS_FOR_PERMANENT_DENIAL = 2

@SuppressLint("InlinedApi")
private fun Activity?.shouldShowRationale(): Boolean =
    this != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_LOCAL_NETWORK)

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
