package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutRemainingFuelLapsDetailPanePreviewTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `保存通知前の変更とリセットでも表示中の閾値で試聴する`() {
        var savedLaps by mutableIntStateOf(3)
        val changed = mutableListOf<Int>()
        val previews = mutableListOf<Pair<String, Int>>()
        var resets = 0
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        AceWindowsReadoutRemainingFuelLapsDetailUiState(
                            remainingFuelLaps = savedLaps,
                            isTextToSpeechAvailable = true,
                        ),
                    onRemainingFuelLapsChanged = { changed += it },
                    onReadoutTextPreviewClicked = { text, laps -> previews += text to laps },
                    onResetRemainingFuelLaps = { resets++ },
                )
            }
        }
        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(3f, 1f..5f, 3)))
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        rule.onNodeWithText("残り約: 5 周").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        assertEquals(listOf(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT to 5), previews)
        assertEquals(listOf(5), changed)
        assertEquals(3, savedLaps)

        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(5f, 1f..5f, 3)))
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT to 4, previews.last())
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[2].performScrollTo().performClick()
        rule.onNodeWithText("残り約: 3 周").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT to 3, previews.last())
        assertEquals(1, resets)

        rule.runOnIdle { savedLaps = 2 }
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT to 2, previews.last())
    }
}
