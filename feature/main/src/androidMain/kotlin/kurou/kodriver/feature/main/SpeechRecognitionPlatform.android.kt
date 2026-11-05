package kurou.kodriver.feature.main

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。Androidでは表示する。 */
actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = true

/** isMicrophoneInputSupportedOnPlatform のこのプラットフォーム向け実装。Androidでは音声認識のカードを表示するため false。 */
actual fun isMicrophoneInputSupportedOnPlatform(): Boolean = false

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。実行時の権限ダイアログを表示する。 */
@Composable
actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return { launcher.launch(Manifest.permission.RECORD_AUDIO) }
}
