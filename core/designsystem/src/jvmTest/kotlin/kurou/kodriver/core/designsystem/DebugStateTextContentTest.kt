package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateTextContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `見出し値は文字列があればそのまま表示する`() {
        rule.setContent { KoDriverTheme { DebugStateHeadlineValueContent("1:23.456") } }

        rule.onNodeWithText("1:23.456").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `見出し値はnullなら未取得を表示する`() {
        rule.setContent { KoDriverTheme { DebugStateHeadlineValueContent(null) } }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `テキスト値は文字列があればそのまま表示する`() {
        rule.setContent { KoDriverTheme { DebugStateTextValueContent("GT3") } }

        rule.onNodeWithText("GT3").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `テキスト値はnullと空文字なら未取得を表示する`() {
        val text = mutableStateOf<String?>(null)
        rule.setContent { KoDriverTheme { DebugStateTextValueContent(text.value) } }
        rule.onNodeWithText("未取得").assertIsDisplayed()

        text.value = ""
        rule.waitForIdle()

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `フェード表示は現在の状態の内容を表示しキーが同じなら状態更新でも内容を維持する`() {
        val state = mutableStateOf("A1")
        val keys = mutableListOf<String>()
        rule.setContent {
            DebugStateFadeContent(
                state = state.value,
                contentKey = {
                    keys += it
                    it.take(1)
                },
            ) { Text(text = "表示 $it") }
        }
        rule.onNodeWithText("表示 A1").assertIsDisplayed()

        state.value = "B2"
        rule.waitForIdle()

        rule.onNodeWithText("表示 B2").assertIsDisplayed()
        assertEquals(true, keys.contains("B2"))
    }
}
