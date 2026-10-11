package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import org.junit.Rule
import org.junit.Test

/** 全10種の入力欄の既定文言がログ用既定文言と一致することを検証する。 */
class AceWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全10種の入力欄の既定値がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        listOf(
            AceWindowsWhiteFlag().narratedText,
            AceWindowsGreenFlag().narratedText,
            AceWindowsRedFlag().narratedText,
            AceWindowsBlueFlag().narratedText,
            AceWindowsYellowFlag().narratedText,
            AceWindowsBlackFlag().narratedText,
            AceWindowsBlackWhiteFlag().narratedText,
            AceWindowsCheckeredFlag().narratedText,
            AceWindowsOrangeCircleFlag().narratedText,
            AceWindowsRedYellowStripesFlag().narratedText,
        ).forEachIndexed { index, narratedText ->
            rule
                .onAllNodesWithText(narratedText)[if (index < 8) 1 else 0]
                .assertTextContains(narratedText, substring = true)
        }
    }
}
