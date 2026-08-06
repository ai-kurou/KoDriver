package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneTest {
    @Test
    fun `デフォルト値3周のスライダーと説明を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent()
                }
            }

            onNodeWithText("現在の最速ラップの30秒前にあたるタイミングで判定し", substring = true)
                .assertIsDisplayed()
            onNodeWithText("残り約: 3 周").assertIsDisplayed()
            onNodeWithText("燃料残り周回数").assertIsDisplayed()
            onNodeWithText("燃料は残り約3周・燃料がありません")
                .assertIsDisplayed()
                .assertIsSelected()
        }

    @Test
    fun `スライダーに1周を表示できる`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                        uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 1),
                        onRemainingFuelLapsChanged = {},
                    )
                }
            }

            onNodeWithText("残り約: 1 周").assertIsDisplayed()
            onNodeWithText("燃料は残り約1周・燃料がありません")
                .assertIsDisplayed()
                .assertIsSelected()
        }

    @Test
    fun `スライダーの値を確定するとonRemainingFuelLapsChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedRemainingFuelLaps: Int? = null
            setContent {
                MaterialTheme {
                    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                        uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 3),
                        onRemainingFuelLapsChanged = { changedRemainingFuelLaps = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(5f)
            }

            assertEquals(5, changedRemainingFuelLaps)
        }

    @Test
    fun `リセットボタンをタップするとonResetRemainingFuelLapsが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                MaterialTheme {
                    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                        uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 5),
                        onRemainingFuelLapsChanged = {},
                        onResetRemainingFuelLaps = { resetCalled = true },
                    )
                }
            }

            onNode(hasContentDescription("デフォルト値にリセット")).performClick()

            assertTrue(resetCalled)
        }

    @Test
    fun `チップをタップするとonPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var previewClicked = false
            setContent {
                MaterialTheme {
                    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                        onPreviewClicked = { previewClicked = true },
                    )
                }
            }

            onNodeWithText("燃料は残り約3周・燃料がありません").performClick()

            assertTrue(previewClicked)
        }
}
