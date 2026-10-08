package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test

/** 自由文言の初期表示はドメインの既定値と一致する。 */
class AceWindowsReadoutRemainingFuelDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力欄が燃料残量警告の既定文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutRemainingFuelDetailPaneContent()
            }
        }

        val narratedText = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
        rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
    }
}
