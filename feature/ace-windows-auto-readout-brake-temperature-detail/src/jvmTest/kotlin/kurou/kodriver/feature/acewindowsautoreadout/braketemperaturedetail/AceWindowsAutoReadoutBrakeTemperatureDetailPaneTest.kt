package kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsAutoReadoutBrakeTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文を表示する`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPane()
            }
        }

        rule.onNodeWithText("ブレーキ温度のお知らせは準備中です。現在は読み上げを行いません。").assertIsDisplayed()
    }
}
