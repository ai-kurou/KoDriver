package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップでは音声認識を提供しない。 */
actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = false

/** isMicrophoneInputSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップではマイク入力の確認用カードを表示する。 */
actual fun isMicrophoneInputSupportedOnPlatform(): Boolean = true

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit = {}
