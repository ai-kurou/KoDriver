package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class Gt7Ps5ReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とタイトルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤの温度状況を音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("タイヤ温度").assertIsDisplayed()
    }
}
