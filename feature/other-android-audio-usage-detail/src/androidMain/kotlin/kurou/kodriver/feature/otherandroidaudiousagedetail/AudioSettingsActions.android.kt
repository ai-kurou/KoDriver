package kurou.kodriver.feature.otherandroidaudiousagedetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** rememberOpenVolumePanel のこのプラットフォーム向け実装。 */
@Composable
actual fun rememberOpenVolumePanel(): (() -> Unit)? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val context = LocalContext.current
    return {
        try {
            context.startActivity(Intent(Settings.Panel.ACTION_VOLUME))
        } catch (_: ActivityNotFoundException) {
            // 一部端末では音量パネルが提供されない。
        }
    }
}

/** rememberOpenSoundSettings のこのプラットフォーム向け実装。 */
@Composable
actual fun rememberOpenSoundSettings(): (() -> Unit)? {
    val context = LocalContext.current
    return {
        try {
            context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            // 一部端末ではサウンド設定画面が提供されない。
        }
    }
}
