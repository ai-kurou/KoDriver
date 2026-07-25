package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutPitTimingDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカードタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent()
            }
        }

        rule.onNodeWithText(
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、いずれかが閾値以下であれば読み上げます。",
        ).assertIsDisplayed()
        rule.onNodeWithText("予想残り周回数").assertIsDisplayed()
    }

    @Test
    fun `バーチャルエナジーのスイッチをタップするとコールバックが呼ばれる`() {
        var virtualEnergyEnabled = true
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyEnabled = virtualEnergyEnabled),
                    onVirtualEnergyEnabledChanged = { virtualEnergyEnabled = it },
                )
            }
        }

        rule.onNodeWithText("予想残り周回数").performClick()

        assert(!virtualEnergyEnabled)
    }
}
