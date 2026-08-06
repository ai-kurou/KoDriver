package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreTemperatureDetailPaneTest {
    @Test
    fun `リセットボタンをクリックするとonHighThresholdResetが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
                        onHighThresholdReset = { resetCalled = true },
                    )
                }
            }
            onNodeWithContentDescription("デフォルトに戻す").performClick()
            assertEquals(true, resetCalled)
        }

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                    )
                }
            }

            onNodeWithContentDescription("高温閾値の説明を表示").performClick()

            onNodeWithText("設定した温度以上になると過熱警告を読み上げます", substring = true).assertIsDisplayed()
        }

    @Test
    fun `スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedValue: Int? = null
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 90),
                        onHighThresholdChanged = { changedValue = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..100f, steps = 9)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(95f) }

            assertEquals(95, changedValue)
        }

    @Test
    fun `過熱警告カードのヘッダーをタップするとonOverheatWarningEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedEnabled: Boolean? = null
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                        onOverheatWarningEnabledChanged = { changedEnabled = it },
                    )
                }
            }

            onNodeWithText("過熱警告").performClick()

            assertEquals(false, changedEnabled)
        }

    @Test
    fun `タイヤ過熱警告チップをタップするとonPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var previewClicked = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                        onPreviewClicked = { previewClicked = true },
                    )
                }
            }

            onAllNodesWithText("タイヤ過熱警告", substring = true)[0].performClick()

            assertEquals(true, previewClicked)
        }

    @Test
    fun `タイヤ低温警告チップをタップするとonLowWarningPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var previewClicked = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                        onLowWarningPreviewClicked = { previewClicked = true },
                    )
                }
            }

            onAllNodesWithText("タイヤ低温警告", substring = true)[0].performClick()

            assertEquals(true, previewClicked)
        }

    @Test
    fun `タイヤ低温警告チップが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(lowWarningEnabled = true),
                    )
                }
            }

            onAllNodesWithText("タイヤ低温警告", substring = true)[0].assertIsDisplayed()
        }
}
