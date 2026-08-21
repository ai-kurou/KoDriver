package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutVehicleApproachDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカードタイトルとしきい値が表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent()
            }
        }

        rule.onNodeWithText("周囲の車両が接近した際に音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("接近開始時に読み上げる").assertIsDisplayed()
        rule.onNodeWithText("しきい値").assertIsDisplayed()
    }

    @Test
    fun `カードをタップするとonStartReadoutEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    startReadoutEnabled = true,
                    onStartReadoutEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("接近開始時に読み上げる").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `しきい値スライダーの値を確定するとonThresholdChangedが呼ばれる`() {
        var changedMeters: Double? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    onThresholdChanged = { changedMeters = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 10f, range = 5f..20f, steps = 149)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(15f)
            }

        assertEquals(15.0, changedMeters)
    }

    @Test
    fun `デフォルト値から変更している場合にしきい値のリセットボタンをタップするとonResetThresholdが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    thresholdMeters = 15.0,
                    onResetThreshold = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `Pane単体で状態を保持しスイッチとスライダーの操作が反映される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutVehicleApproachDetailPane()
            }
        }

        rule.onNodeWithText("周囲の車両が接近した際に音声でお知らせします。").assertIsDisplayed()

        rule.onNodeWithText("接近開始時に読み上げる").performClick()
        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 10f, range = 5f..20f, steps = 149)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(15f)
            }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 15f, range = 5f..20f, steps = 149)),
            ).assertIsDisplayed()
    }
}
