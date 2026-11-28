package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップでは音声認識のカードを表示する。 */
internal actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = true

/** isSpeechRecognitionStoppedInBackgroundOnPlatform のこのプラットフォーム向け実装。デスクトップは最小化中も聞き取りを続けるため false。 */
internal actual fun isSpeechRecognitionStoppedInBackgroundOnPlatform(): Boolean = false

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
internal actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit = {}
