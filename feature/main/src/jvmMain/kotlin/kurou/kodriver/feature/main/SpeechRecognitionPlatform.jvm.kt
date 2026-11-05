package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップでは音声認識のカードを表示する。 */
actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = true

/** isMicrophoneInputSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップでは音声認識のカードに加えて、マイク入力の確認用カードも表示する。 */
actual fun isMicrophoneInputSupportedOnPlatform(): Boolean = true

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit = {}
