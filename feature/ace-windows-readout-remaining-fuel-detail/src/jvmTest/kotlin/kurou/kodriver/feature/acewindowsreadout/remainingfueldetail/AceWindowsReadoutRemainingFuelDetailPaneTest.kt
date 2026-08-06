package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutRemainingFuelDetailPaneTest {
    @Test
    fun `説明文とカード内の閾値項目とデフォルト値のスライダーラベルが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    AceWindowsReadoutRemainingFuelDetailPaneContent()
                }
            }

            onNodeWithText("残り燃料が設定した閾値を下回った場合に、音声でお知らせします。").assertIsDisplayed()
            onNodeWithText("残り燃料警告").assertIsDisplayed()
            onNodeWithText("残量閾値").assertIsDisplayed()
            onNodeWithText("残り燃料がこの割合を下回ったら警告を読み上げます。").assertIsDisplayed()
            onNodeWithText("30%").assertIsDisplayed()
        }

    @Test
    fun `チップをタップするとonPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var previewClicked = false
            setContent {
                KoDriverTheme {
                    AceWindowsReadoutRemainingFuelDetailPaneContent(
                        onPreviewClicked = { previewClicked = true },
                    )
                }
            }

            onNodeWithText("残り燃料警告").performClick()

            assertEquals(true, previewClicked)
        }

    @Test
    fun `スライダーの値を確定するとonThresholdChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedPercentage: Int? = null
            setContent {
                KoDriverTheme {
                    AceWindowsReadoutRemainingFuelDetailPaneContent(
                        onThresholdChanged = { changedPercentage = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 30f, range = 5f..90f, steps = 84)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(60f)
            }

            assertEquals(60, changedPercentage)
        }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                KoDriverTheme {
                    AceWindowsReadoutRemainingFuelDetailPaneContent(
                        uiState = AceWindowsReadoutRemainingFuelDetailUiState(thresholdPercentage = 60),
                        onThresholdReset = { resetCalled = true },
                    )
                }
            }

            onNode(hasContentDescription("デフォルト値にリセット")).performClick()

            assertEquals(true, resetCalled)
        }
}
