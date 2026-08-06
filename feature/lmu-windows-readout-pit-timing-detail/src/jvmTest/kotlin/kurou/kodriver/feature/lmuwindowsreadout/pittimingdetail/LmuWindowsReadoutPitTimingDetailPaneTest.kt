package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
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

class LmuWindowsReadoutPitTimingDetailPaneTest {
    @Test
    fun `説明文とカードタイトルを表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutPitTimingDetailPaneContent()
                }
            }

            onNodeWithText(
                "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                    "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、" +
                    "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。",
            ).assertIsDisplayed()
            onNodeWithText("予想残り周回数").assertIsDisplayed()
            onNodeWithText("バーチャルエナジー予想残り周回数").assertIsDisplayed()
            onNodeWithText("タイヤ摩耗予想残り周回数").assertIsDisplayed()
            onNodeWithText("N周以内にピットイン・必ずピットイン").assertIsDisplayed()
            onAllNodesWithText("残り約: 3 周").assertCountEquals(2)
        }

    @Test
    fun `チップをタップするとコールバックが呼ばれる`() =
        composeScreenshotTest {
            var previewClicked = false
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutPitTimingDetailPaneContent(
                        onPreviewClicked = { previewClicked = true },
                    )
                }
            }

            onNodeWithText("N周以内にピットイン・必ずピットイン").performClick()

            assert(previewClicked)
        }

    @Test
    fun `バーチャルエナジーのスライダーを動かすとコールバックが呼ばれる`() =
        composeScreenshotTest {
            var virtualEnergyLaps = 3
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutPitTimingDetailPaneContent(
                        uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyLaps = virtualEnergyLaps),
                        onVirtualEnergyLapsChanged = { virtualEnergyLaps = it },
                    )
                }
            }

            onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            )[0]
                .performSemanticsAction(SemanticsActions.SetProgress) {
                    it(5f)
                }

            assert(virtualEnergyLaps == 5)
        }

    @Test
    fun `タイヤ摩耗のスライダーを動かすとコールバックが呼ばれる`() =
        composeScreenshotTest {
            var tyreWearLaps = 3
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutPitTimingDetailPaneContent(
                        uiState = LmuWindowsReadoutPitTimingDetailUiState(tyreWearLaps = tyreWearLaps),
                        onTyreWearLapsChanged = { tyreWearLaps = it },
                    )
                }
            }

            onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            )[1]
                .performSemanticsAction(SemanticsActions.SetProgress) {
                    it(1f)
                }

            assert(tyreWearLaps == 1)
        }

    @Test
    fun `ヘルプアイコンをタップするとヘルプシートが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    LmuWindowsReadoutPitTimingDetailPaneContent()
                }
            }

            onNodeWithContentDescription("バーチャルエナジー予想残り周回数の計算方法の説明を表示").performClick()

            onNodeWithText("直近1周分の消費量", substring = true).assertIsDisplayed()
        }
}
