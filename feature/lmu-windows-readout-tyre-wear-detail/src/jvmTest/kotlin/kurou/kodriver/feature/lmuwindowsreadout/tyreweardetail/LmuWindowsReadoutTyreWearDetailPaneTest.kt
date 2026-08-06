package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreWearDetailPaneTest {
    @Test
    fun `説明文が表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreWearDetailPaneContent()
                }
            }

            onNodeWithText(
                "タイヤの摩耗率が設定した閾値以上になった場合に音声でお知らせします。" +
                    "いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値未満に戻るまでは再度読み上げません。",
            ).assertIsDisplayed()
        }

    @Test
    fun `摩耗警告カードとデフォルトONのタイヤ摩耗警告チップが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreWearDetailPaneContent()
                }
            }

            onNodeWithText("摩耗警告").assertIsDisplayed()
            onNodeWithText("タイヤ摩耗警告")
                .assertIsDisplayed()
                .assertIsSelected()
        }

    @Test
    fun `タイヤ摩耗警告チップをタップするとonWarningChipClickedが呼ばれる`() =
        composeScreenshotTest {
            var clicked = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreWearDetailPaneContent(
                        onWarningChipClicked = { clicked = true },
                    )
                }
            }

            onNodeWithText("タイヤ摩耗警告").performClick()

            assertEquals(true, clicked)
        }

    @Test
    fun `摩耗閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreWearDetailPaneContent()
                }
            }

            onNodeWithText("摩耗閾値").assertIsDisplayed()
            onNodeWithText("この閾値になると警告を読み上げます").assertIsDisplayed()
            onNodeWithText("50%").assertIsDisplayed()
        }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutTyreWearDetailPaneContent(
                        uiState = LmuWindowsReadoutTyreWearDetailUiState(thresholdPercentage = 30),
                        onThresholdReset = { resetCalled = true },
                    )
                }
            }

            onNodeWithContentDescription("デフォルトに戻す").performClick()

            assertEquals(true, resetCalled)
        }
}
