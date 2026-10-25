package kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsAutoReadoutPitTimingDetailPaneSliderPreviewTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `バーチャルエネルギーのスライダー変更がuiStateに反映される前でも表示中の周回数で試聴する`() {
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutPitTimingDetailUiState(
                            virtualEnergyLaps = 3,
                            virtualEnergyText = "残り{laps}周",
                            isTextToSpeechAvailable = true,
                        ),
                    onVirtualEnergyTextPreviewClicked = { text, laps -> previews += text to laps },
                )
            }
        }

        rule
            .onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            )[0]
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) {
                it(1f)
            }
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()

        assertEquals(listOf("残り{laps}周" to 1), previews)
    }

    @Test
    fun `タイヤ摩耗の周回数をデフォルトに戻すとuiStateに反映される前でもデフォルトの周回数で試聴する`() {
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutPitTimingDetailUiState(
                            virtualEnergyLaps = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                            tyreWearLaps = 5,
                            isTextToSpeechAvailable = true,
                        ),
                    onTyreWearTextPreviewClicked = { text, laps -> previews += text to laps },
                )
            }
        }

        rule
            .onAllNodes(hasContentDescription("デフォルトに戻す") and isEnabled())[0]
            .performScrollTo()
            .performClick()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performScrollTo().performClick()

        assertEquals(
            listOf(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT to LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
            ),
            previews,
        )
    }

    @Test
    fun `燃料の周回数をデフォルトに戻すとuiStateに反映される前でもデフォルトの周回数で試聴する`() {
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutPitTimingDetailUiState(
                            virtualEnergyLaps = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                            fuelLaps = 5,
                            isTextToSpeechAvailable = true,
                        ),
                    onFuelTextPreviewClicked = { text, laps -> previews += text to laps },
                )
            }
        }

        rule
            .onAllNodes(hasContentDescription("デフォルトに戻す") and isEnabled())[0]
            .performScrollTo()
            .performClick()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[4].performScrollTo().performClick()

        assertEquals(
            listOf(
                LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT to LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT,
            ),
            previews,
        )
    }
}
