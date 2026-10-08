package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * UI は「燃料残量」に統一する一方、既存 WAV と [SpeechEvent.narratedText] は変更しない（#1784）。
 * 表示文言と音声の記録文言をそれぞれ検証する。
 */
class AceWindowsReadoutRemainingFuelDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chipは燃料残量警告を表示し既存音声の記録文言は維持される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("燃料残量警告").assertIsDisplayed()
        assertEquals("燃料は残り20パーセント", SpeechEvent.AceWindowsRemainingFuelWarning(20).narratedText)
    }
}
