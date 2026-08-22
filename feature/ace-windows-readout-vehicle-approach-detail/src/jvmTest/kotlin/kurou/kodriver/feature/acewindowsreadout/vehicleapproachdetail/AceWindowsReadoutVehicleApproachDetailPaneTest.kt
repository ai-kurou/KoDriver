package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutVehicleApproachDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文と閾値設定と接近開始時の読み上げカードが表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent()
            }
        }

        rule.onNodeWithText("周囲の車両が接近した際に音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("閾値設定").assertIsDisplayed()
        rule.onNodeWithText("車両間隔: 6.0 m").assertIsDisplayed()
        rule.onNodeWithText("接近開始時の読み上げ").assertIsDisplayed()
        rule.onNodeWithText("車両接近").assertIsDisplayed()
    }

    @Test
    fun `ヘルプアイコンをタップするとヘルプ説明が表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent()
            }
        }

        rule.onNode(hasContentDescription("閾値の説明を表示")).performClick()

        rule
            .onNodeWithText(
                "閾値は自車中心から相手車両中心までの距離です。この範囲内に入ると車両接近と判定され読み上げられます。\n\n" +
                    "※車両は一般的に全長 5 m 以下、全幅 2 m 以下となっています。",
            ).assertIsDisplayed()
    }

    @Test
    fun `閾値スライダーの値を確定するとonThresholdChangedが呼ばれる`() {
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
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 6f, range = 2f..10f, steps = 79)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(7f)
            }

        assertEquals(7.0, changedMeters)
    }

    @Test
    fun `閾値のリセットボタンをタップするとonResetThresholdが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(thresholdMeters = 7.0),
                    onResetThreshold = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `カードをタップするとonStartReadoutEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = true),
                    onStartReadoutEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("接近開始時の読み上げ").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `プレビューチップをタップするとonPreviewClickedが呼ばれる`() {
        var previewCount = 0
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = true),
                    onPreviewClicked = { previewCount++ },
                )
            }
        }

        rule.onNodeWithText("車両接近").assertIsEnabled().performClick()

        assertEquals(1, previewCount)
    }

    @Test
    fun `読み上げが無効ならチップも無効になる`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = false),
                )
            }
        }

        rule.onNodeWithText("車両接近").assertIsNotEnabled()
    }
}
