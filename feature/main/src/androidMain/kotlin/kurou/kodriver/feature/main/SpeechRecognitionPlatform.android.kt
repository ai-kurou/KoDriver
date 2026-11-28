package kurou.kodriver.feature.main

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。Androidでは表示する。 */
internal actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = true

/** isSpeechRecognitionStoppedInBackgroundOnPlatform のこのプラットフォーム向け実装。Androidは背面化で聞き取りを止めるため true。 */
internal actual fun isSpeechRecognitionStoppedInBackgroundOnPlatform(): Boolean = true

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。実行時の権限ダイアログを表示する。 */
@Composable
internal actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return { launcher.launch(Manifest.permission.RECORD_AUDIO) }
}
