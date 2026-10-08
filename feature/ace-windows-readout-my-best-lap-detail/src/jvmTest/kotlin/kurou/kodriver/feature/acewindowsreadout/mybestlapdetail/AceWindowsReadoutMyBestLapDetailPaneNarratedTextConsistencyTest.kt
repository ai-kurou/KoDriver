package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI の文言入力欄ラベルと、試聴サンプルのタイムを展開した TTS 記録文言を検証する。
 * 表示文言と音声の記録文言をそれぞれ検証する。
 */
class AceWindowsReadoutMyBestLapDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `文言入力欄は自己ベストラップ更新の文言を表示し試聴文言にはサンプルのタイムを含む`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutMyBestLapDetailPaneContent()
            }
        }

        rule.onNodeWithText("自己ベストラップ更新の文言").assertIsDisplayed()
        assertEquals("自己ベストラップ更新 1分23秒456", SpeechEvent.AceWindowsMyBestLap(83_456).narratedText)
    }
}
