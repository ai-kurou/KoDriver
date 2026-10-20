package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherAndroidAudioUsageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明を表示し戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                OtherAndroidAudioUsageDetailPane(canNavigateBack = true, onBack = { backCount++ })
            }
        }

        rule.onNodeWithText("音声の用途").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }
}
