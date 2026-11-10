package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

internal const val SPEECH_RECOGNITION_LIST_PANE_TEST_TAG = "speech_recognition_list_pane"

/**
 * 「音声」タブの一覧（listPane）。現時点では中身のない空の画面で、音声認識の設定・状態の表示は後続の変更で追加する。
 */
@Composable
fun SpeechRecognitionListPane(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().testTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG))
}
