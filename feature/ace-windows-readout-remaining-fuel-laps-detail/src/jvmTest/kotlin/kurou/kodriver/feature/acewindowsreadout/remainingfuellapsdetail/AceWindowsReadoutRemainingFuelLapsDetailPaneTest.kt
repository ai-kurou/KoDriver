package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutRemainingFuelLapsDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutRemainingFuelLapsDetailPane()
            }
        }

        rule.onNodeWithText("燃料残り周回数").assertIsDisplayed()
        rule
            .onNodeWithText(
                "各ラップごとに燃料と走行可能な残り周回数を計算します。現在のベストラップの30秒前にあたるタイミングで判定し、" +
                    "設定した周回数以下になると音声でお知らせします。",
            ).assertIsDisplayed()
    }
}
