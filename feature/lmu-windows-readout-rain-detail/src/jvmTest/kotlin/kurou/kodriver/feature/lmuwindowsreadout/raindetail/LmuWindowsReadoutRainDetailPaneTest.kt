package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutRainDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutRainDetailPane()
            }
        }

        rule.onNodeWithText("セッション開始時に降雨の予報を音声でお知らせします。").assertIsDisplayed()
    }
}
