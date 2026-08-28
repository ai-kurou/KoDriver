package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutMyBestLapDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutMyBestLapDetailPane()
            }
        }

        rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
        rule.onNodeWithText("自己ベストラップを更新した場合に、音声でお知らせします。").assertIsDisplayed()
    }
}
