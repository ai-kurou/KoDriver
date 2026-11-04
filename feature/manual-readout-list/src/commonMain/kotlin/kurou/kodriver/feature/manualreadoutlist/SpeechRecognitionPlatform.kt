package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.Composable

/**
 * 音声認識の確認用カードを表示するプラットフォームか。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
expect fun isSpeechRecognitionSupportedOnPlatform(): Boolean

/**
 * マイクの権限を要求する関数を取得する。結果は [onResult] で受ける。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
expect fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
