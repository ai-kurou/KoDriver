package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable

/** isSpeechRecognitionSupportedOnPlatform のこのプラットフォーム向け実装。デスクトップでは音声認識を提供しない。 */
actual fun isSpeechRecognitionSupportedOnPlatform(): Boolean = false

/** rememberRecordAudioPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
actual fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit = {}
