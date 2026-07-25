package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutPitTimingDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPane()
            }
        }

        rule.onNodeWithText("ピットインの最適なタイミングが近づいたときに音声でお知らせします。").assertIsDisplayed()
    }
}
