package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

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
    fun `フェード表示はキーが同じなら内容を切り替えずに更新しキーが変わるとフェードで切り替える`() {
        val state = mutableStateOf("A1")
        rule.setContent {
            DebugStateFadeContent(
                state = state.value,
                contentKey = { it.take(1) },
            ) { Text(text = "表示 $it") }
        }
        rule.onNodeWithText("表示 A1").assertIsDisplayed()

        // キーが同じ間は遷移せず、古い内容と新しい内容が同時に存在しない。
        rule.mainClock.autoAdvance = false
        state.value = "A2"
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("表示 A1").assertDoesNotExist()
        rule.onNodeWithText("表示 A2").assertIsDisplayed()

        // キーが変わると遷移中は古い内容と新しい内容が同時に存在する。
        state.value = "B1"
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("表示 A2").assertExists()
        rule.onNodeWithText("表示 B1").assertExists()

        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithText("表示 A2").assertDoesNotExist()
        rule.onNodeWithText("表示 B1").assertIsDisplayed()
    }
}
