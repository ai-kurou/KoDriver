package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI の「燃料残量」表示と、試聴サンプルの残量を展開した TTS 記録文言を検証する。
 * 表示文言と音声の記録文言をそれぞれ検証する。
 */
class Gt7Ps5ReadoutRemainingFuelDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chipは燃料残量警告を表示し試聴文言にはサンプルの残量を含む`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("燃料残量警告").assertIsDisplayed()
        assertEquals("燃料は残り30パーセント", SpeechEvent.Gt7Ps5RemainingFuelWarning(30).narratedText)
    }
}
