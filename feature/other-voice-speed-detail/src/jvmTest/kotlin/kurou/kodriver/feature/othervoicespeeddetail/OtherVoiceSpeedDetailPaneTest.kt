package kurou.kodriver.feature.othervoicespeeddetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherVoiceSpeedDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明を表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPane(canNavigateBack = true, onBack = { backCount++ })
            }
        }

        rule.onNodeWithText("読み上げ速度").assertIsDisplayed()
        rule.onNodeWithText("読み上げの速さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPane(canNavigateBack = false, onBack = {})
            }
        }

        rule.onNodeWithText("読み上げ速度").assertIsDisplayed()
        rule.onNodeWithText("読み上げの速さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }
}
