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
 * 通常は権限のダイアログを出す。要求を拒否されたあとで再び要求の根拠を示す画面が出ない（恒久的な拒否）場合は、
 * 次回から設定画面を開く。
 *
 * ACCESS_LOCAL_NETWORK は API 37 で追加された定数だが、コンパイル時に文字列へインライン展開されるため
 * 古い端末で参照しても実行時エラーにならない。権限が必要な端末（未許可時のみバナーから呼ばれる）でしか使われない。
 */
@SuppressLint("InlinedApi")
@Composable
actual fun rememberAccessLocalNetworkPermissionRequester(onResult: () -> Unit): () -> Unit {
    val activity = LocalContext.current.findActivity()
    val currentOnResult by rememberUpdatedState(onResult)
    val openSettings = rememberOpenAccessLocalNetworkPermissionSettings()
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permanentlyDenied =
                !granted &&
                activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_LOCAL_NETWORK,
                )
            currentOnResult()
        }
    return remember(launcher, openSettings) {
        {
            if (permanentlyDenied) {
                openSettings()
            } else {
                launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
