package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRemainingVirtualEnergyDetailPaneTest {
    @Test
    fun `説明文が表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
                }
            }

            onNodeWithText("バーチャルエナジー残量が設定した閾値以下になった場合に音声でお知らせします。").assertIsDisplayed()
        }

    @Test
    fun `残量警告カードとデフォルトONの警告チップが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
                }
            }

            onNodeWithText("残量警告").assertIsDisplayed()
            onNodeWithText("バーチャルエナジー残量警告")
                .assertIsDisplayed()
                .assertIsSelected()
        }

    @Test
    fun `警告チップをタップするとonWarningChipClickedが呼ばれる`() =
        composeScreenshotTest {
            var clicked = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                        onWarningChipClicked = { clicked = true },
                    )
                }
            }

            onNodeWithText("バーチャルエナジー残量警告").performClick()

            assertEquals(true, clicked)
        }

    @Test
    fun `閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
                }
            }

            onNodeWithText("残量閾値").assertIsDisplayed()
            onNodeWithText("この閾値になると警告を読み上げます").assertIsDisplayed()
            onNodeWithText("30%").assertIsDisplayed()
        }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                        uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(thresholdPercentage = 50),
                        onThresholdReset = { resetCalled = true },
                    )
                }
            }

            onNodeWithContentDescription("デフォルトに戻す").performClick()

            assertEquals(true, resetCalled)
        }
}
