package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test

/** 自由文言の初期表示はドメインの既定値と一致する。 */
class AceWindowsReadoutRemainingFuelLapsDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力欄が通常と燃料なしの既定文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutRemainingFuelLapsDetailPaneContent()
            }
        }

        listOf(
            ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
            ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
        ).forEach { text ->
            rule.onAllNodesWithText(text)[0].assertTextContains(text, substring = true)
        }
    }
}
