package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performScrollTo
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test

/** 自由文言の初期表示はドメインの既定値と一致する。 */
class LmuWindowsReadoutTyreTemperatureDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力欄が過熱と低温の既定文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                )
            }
        }

        listOf(
            LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
            LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
        ).forEach { narratedText ->
            rule
                .onAllNodesWithText(
                    narratedText,
                )[0]
                .performScrollTo()
                .assertTextContains(narratedText, substring = true)
        }
    }
}
