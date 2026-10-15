package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailPaneCardTextFieldInsertTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `中間への同じ文字列の連続挿入は各要求を一度だけ確定して試聴にも反映する`() {
        lateinit var insertState: TextInsertState
        var value by mutableStateOf("abcd")
        val finishedTexts = mutableListOf<String>()
        var previewText: String? = null
        rule.setContent {
            KoDriverTheme {
                insertState = rememberTextInsertState()
                DetailPaneCardTextField(
                    value = value,
                    placeholder = "文言",
                    maxLength = 10,
                    onValueChangeFinished = {
                        finishedTexts.add(it)
                        value = it
                    },
                    onPreviewClick = { previewText = it },
                    previewContentDescription = "試聴",
                    modifier = Modifier.testTag("textField"),
                    insertState = insertState,
                )
            }
        }
        val field = rule.onNodeWithTag("textField")
        field.performTextInputSelection(TextRange(2))
        rule.runOnIdle { insertState.insert("X") }
        rule.waitForIdle()
        assertEquals(listOf("abXcd"), finishedTexts)
        assertEquals(TextRange(3), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        rule.runOnIdle { insertState.insert("X") }
        rule.waitForIdle()
        assertEquals(listOf("abXcd", "abXXcd"), finishedTexts)
        assertNull(insertState.request)
        rule.onNodeWithText("6/10").assertIsDisplayed()
        rule.onNodeWithContentDescription("試聴").performClick()
        assertEquals("abXXcd", previewText)
    }

    @Test
    fun `上限超過の要求は消費して文言も選択範囲も確定通知も変えない`() {
        val insertState = TextInsertState()
        val finishedTexts = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "abcd",
                    placeholder = "文言",
                    maxLength = 4,
                    onValueChangeFinished = { finishedTexts.add(it) },
                    onPreviewClick = null,
                    modifier = Modifier.testTag("textField"),
                    insertState = insertState,
                )
            }
        }
        val field = rule.onNodeWithTag("textField")
        field.performTextInputSelection(TextRange(2))
        rule.runOnIdle { insertState.insert("X") }
        rule.waitForIdle()
        rule.onNodeWithText("abcd").assertIsDisplayed()
        assertEquals(TextRange(2), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        assertEquals(emptyList(), finishedTexts)
        assertNull(insertState.request)

        field.performTextInputSelection(TextRange(3, 1))
        rule.waitForIdle()
        assertEquals(emptyList(), finishedTexts)
        rule.runOnIdle { insertState.insert("X") }
        rule.waitForIdle()
        assertEquals(listOf("aXd"), finishedTexts)
    }

    @Test
    fun `挿入状態を指定しなくても改行除去と切り詰めと外部値の再初期化を維持する`() {
        var value by mutableStateOf("abcd")
        val finishedTexts = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = value,
                    placeholder = "文言",
                    maxLength = 5,
                    onValueChangeFinished = { finishedTexts.add(it) },
                    onPreviewClick = null,
                    modifier = Modifier.testTag("textField"),
                )
            }
        }
        val field = rule.onNodeWithTag("textField")
        assertEquals(TextRange(4), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        field.performTextReplacement("ab\ncdef")
        assertEquals(listOf("abcde"), finishedTexts)
        rule.onNodeWithText("5/5").assertIsDisplayed()
        field.performTextInputSelection(TextRange(2))
        rule.runOnIdle { value = "uvwxyz" }
        rule.waitForIdle()
        rule.onNodeWithText("uvwxyz").assertIsDisplayed()
        assertEquals(TextRange(2), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        rule.runOnIdle { value = "a" }
        rule.waitForIdle()
        assertEquals(TextRange(1), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        assertEquals(listOf("abcde"), finishedTexts)
    }
}
