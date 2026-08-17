package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤ温度").assertIsDisplayed()
        rule
            .onNodeWithText(
                "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、" +
                    "ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
            ).assertIsDisplayed()
    }
}
