package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreWearDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定文言の入力欄とイベントの閾値置換が一致する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreWearDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertTextContains(LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT)
        assertEquals(
            formatLmuWindowsTyreWearReadoutText(
                LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
            ),
            LmuWindowsTyreWearWarning(LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT).narratedText,
        )
    }
}
