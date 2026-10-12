package kurou.kodriver.core.designsystem

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugOverlayTextInputCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力と選択と送信を通知し空白では送信できない`() {
        val text = mutableStateOf("")
        val selected = mutableStateOf("lmu_windows_my_best_lap")
        var sent = 0
        val keys = listOf("lmu_windows_my_best_lap", "gt7_ps5_my_best_lap")
        rule.setContent {
            KoDriverTheme {
                DebugOverlayTextInputCard(
                    text = text.value,
                    selectedKey = selected.value,
                    keys = keys,
                    onTextChange = { text.value = it },
                    onKeySelect = { selected.value = it },
                    onSend = { sent++ },
                )
            }
        }
        rule.onNodeWithText("オーバーレイ表示テスト").assertIsDisplayed()
        rule.onNodeWithText("読み上げ項目").assertIsDisplayed()
        rule.onNodeWithText("送信").assertIsNotEnabled()
        rule.onNodeWithText("表示する文字列").performTextReplacement(" \n\t")
        rule.onNodeWithText("送信").assertIsNotEnabled()
        rule.onNodeWithText("表示する文字列").performTextReplacement("表示テスト")
        rule.onNodeWithText("送信").assertIsEnabled().performClick()
        assertEquals("表示テスト", text.value)
        assertEquals(1, sent)
        rule.onNodeWithText("自己ベストラップ (lmu_windows_my_best_lap)").performClick()
        rule.onNodeWithText("自己ベストラップ (gt7_ps5_my_best_lap)").performClick()
        assertEquals("gt7_ps5_my_best_lap", selected.value)
    }
}
