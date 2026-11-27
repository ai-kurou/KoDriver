package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherWakeWordHotkeyDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private var backCount = 0

    private fun setPane() {
        rule.setContent {
            MaterialTheme {
                OtherWakeWordHotkeyDetailPane(canNavigateBack = true, onBack = { backCount++ })
            }
        }
    }

    @Test
    fun `タイトルと説明を表示する`() {
        setPane()

        rule.onNodeWithText("ウェイクワードホットキー").assertIsDisplayed()
        rule
            .onNodeWithText("ホットキーを押すことで、ウェイクワードを話したときと同じようにコマンドの受付を始める機能の設定です。")
            .assertIsDisplayed()
    }

    @Test
    fun `戻るボタンをタップすると戻る操作を通知する`() {
        setPane()

        rule.onNodeWithContentDescription("戻る").performClick()

        assertEquals(1, backCount)
    }
}
