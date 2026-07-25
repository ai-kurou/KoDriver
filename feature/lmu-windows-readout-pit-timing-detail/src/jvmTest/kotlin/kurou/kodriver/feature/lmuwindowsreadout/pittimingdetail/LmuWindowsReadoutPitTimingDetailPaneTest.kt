package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutPitTimingDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカードタイトルを表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent()
            }
        }

        rule.onNodeWithText(
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、いずれかが閾値以下であれば読み上げます。",
        ).assertIsDisplayed()
        rule.onNodeWithText("予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー予想残り周回数").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗予想残り周回数").assertIsDisplayed()
        rule.onAllNodesWithText("残り約: 3 周").assertCountEquals(2)
    }

    @Test
    fun `バーチャルエナジーのスイッチをタップするとコールバックが呼ばれる`() {
        var virtualEnergyEnabled = true
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyEnabled = virtualEnergyEnabled),
                    onVirtualEnergyEnabledChanged = { virtualEnergyEnabled = it },
                )
            }
        }

        rule.onNodeWithText("予想残り周回数").performClick()

        assert(!virtualEnergyEnabled)
    }

    @Test
    fun `バーチャルエナジーのスライダーを動かすとコールバックが呼ばれる`() {
        var virtualEnergyLaps = 3
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyLaps = virtualEnergyLaps),
                    onVirtualEnergyLapsChanged = { virtualEnergyLaps = it },
                )
            }
        }

        rule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
        )[0].performSemanticsAction(SemanticsActions.SetProgress) {
            it(5f)
        }

        assert(virtualEnergyLaps == 5)
    }

    @Test
    fun `タイヤ摩耗のスライダーを動かすとコールバックが呼ばれる`() {
        var tyreWearLaps = 3
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(tyreWearLaps = tyreWearLaps),
                    onTyreWearLapsChanged = { tyreWearLaps = it },
                )
            }
        }

        rule.onAllNodes(
            hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
        )[1].performSemanticsAction(SemanticsActions.SetProgress) {
            it(1f)
        }

        assert(tyreWearLaps == 1)
    }

    @Test
    fun `ヘルプアイコンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent()
            }
        }

        rule.onNodeWithContentDescription("バーチャルエナジー予想残り周回数の計算方法の説明を表示").performClick()

        rule.onNodeWithText("レース開始からの平均消費量", substring = true).assertIsDisplayed()
    }
}
