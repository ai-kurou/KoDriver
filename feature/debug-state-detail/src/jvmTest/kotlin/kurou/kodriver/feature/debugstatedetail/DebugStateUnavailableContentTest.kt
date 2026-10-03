package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class DebugStateUnavailableContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `スケルトンを添えて既存の未取得文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.width(360.dp)) {
                    DebugStateUnavailableContent()
                }
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }
}
