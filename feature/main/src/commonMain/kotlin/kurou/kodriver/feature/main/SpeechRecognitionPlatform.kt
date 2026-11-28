package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable

/**
 * 音声認識の確認用カードを表示するプラットフォームか。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
internal expect fun isSpeechRecognitionSupportedOnPlatform(): Boolean

/**
 * アプリが見えなくなったとき（背面化・最小化）に音声認識を止めるプラットフォームか。
 * true のプラットフォームでは見えなくなると停止し、前面に戻ると見えなくなる前に聞き取り中だった場合だけ再開する。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
internal expect fun isSpeechRecognitionStoppedInBackgroundOnPlatform(): Boolean

/**
 * マイクの権限を要求する関数を取得する。結果は [onResult] で受ける。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
internal expect fun rememberRecordAudioPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
