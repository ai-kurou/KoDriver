package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class PlaceholderReadoutTextFieldTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力と試聴は保存反映前の現在の文言を一度通知する`() {
        val changedTexts = mutableListOf<String>()
        val previewTexts = mutableListOf<String>()
        setContent(onTextChanged = { changedTexts.add(it) }, onPreviewClick = { previewTexts.add(it) })

        rule.onNode(editableTextField).performTextReplacement("編集した文言")
        rule.onNodeWithContentDescription("試聴").performClick()

        rule.onNode(editableTextField).assertEditableText("編集した文言")
        assertEquals(listOf("編集した文言"), changedTexts)
        assertEquals(listOf("編集した文言"), previewTexts)
    }

    @Test
    fun `チップで上限ちょうどまで挿入でき次の挿入は無効になる`() {
        val changedTexts = mutableListOf<String>()
        setContent(savedText = "注意", maxLength = 11, onTextChanged = { changedTexts.add(it) })

        rule.onNodeWithText("ラップタイムを挿入").assertIsEnabled().performClick()

        rule.onNode(editableTextField).assertEditableText("注意{laptime}")
        rule.onNodeWithText("ラップタイムを挿入").assertIsNotEnabled().performClick()
        assertEquals(listOf("注意{laptime}"), changedTexts)
    }

    @Test
    fun `最大長超過でチップは無効になり入力を短くすると有効になる`() {
        val changedTexts = mutableListOf<String>()
        setContent(savedText = "注意", maxLength = 10, onTextChanged = { changedTexts.add(it) })

        rule.onNodeWithText("ラップタイムを挿入").assertIsNotEnabled().performClick()
        assertEquals(emptyList(), changedTexts)
        rule.onNode(editableTextField).performTextReplacement("注")

        rule.onNodeWithText("ラップタイムを挿入").assertIsEnabled()
        assertEquals(listOf("注"), changedTexts)
    }

    @Test
    fun `TTS不可でチップは無効になり文言を通知しない`() {
        val changedTexts = mutableListOf<String>()
        setContent(isTextToSpeechAvailable = false, onTextChanged = { changedTexts.add(it) })

        rule.onNodeWithText("ラップタイムを挿入").assertIsNotEnabled().performClick()

        rule.onNodeWithText("読み上げ不可").assertIsDisplayed()
        rule.onNode(editableTextField).assertIsNotEnabled()
        assertEquals(emptyList(), changedTexts)
    }

    @Test
    fun `未知プレースホルダの説明は保存反映前の入力から生成する`() {
        setContent(
            unknownPlaceholdersText = { currentText ->
                if (currentText.contains("{unknown}")) "未知の文言 {unknown}" else null
            },
        )

        rule.onNodeWithText("未知の文言 {unknown}").assertDoesNotExist()
        rule.onNode(editableTextField).performTextReplacement("{unknown}")
        rule.onNodeWithText("未知の文言 {unknown}").assertIsDisplayed()
        rule.onNode(editableTextField).performTextReplacement("注意")
        rule.onNodeWithText("未知の文言 {unknown}").assertDoesNotExist()
    }

    @Test
    fun `リセットで既定文言を一度通知し保存反映前にも表示する`() {
        val changedTexts = mutableListOf<String>()
        setContent(onTextChanged = { changedTexts.add(it) })

        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()

        rule.onNode(editableTextField).assertEditableText("既定の文言")
        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled()
        assertEquals(listOf("既定の文言"), changedTexts)
    }

    private val editableTextField = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)

    @Test
    fun `編集後のリセットは既定文言を表示して一度だけ通知する`() {
        val changedTexts = mutableListOf<String>()
        setContent(onTextChanged = { changedTexts.add(it) })

        rule.onNode(editableTextField).performTextReplacement("編集済み文言")
        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsEnabled().performClick()

        rule.onNode(editableTextField).assertEditableText("既定の文言")
        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled()
        assertEquals(listOf("編集済み文言", "既定の文言"), changedTexts)
    }

    @Test
    fun `既定文言と同じならリセットできず通知しない`() {
        val changedTexts = mutableListOf<String>()
        setContent(savedText = "既定の文言", onTextChanged = { changedTexts.add(it) })

        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled().performClick()

        assertEquals(emptyList(), changedTexts)
    }

    @Test
    fun `TTS不可では編集済み文言をリセットできず通知しない`() {
        val changedTexts = mutableListOf<String>()
        setContent(
            savedText = "編集済み文言",
            isTextToSpeechAvailable = false,
            onTextChanged = { changedTexts.add(it) },
        )

        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled().performClick()

        rule.onNode(editableTextField).assertEditableText("編集済み文言")
        assertEquals(emptyList(), changedTexts)
    }

    @Test
    fun `保存待ちのリセットは古い保存値で巻き戻らず保存完了後は外部更新に追従する`() {
        var saved by mutableStateOf("注意")
        setContent(savedTextProvider = { saved })

        rule.onNode(editableTextField).performTextReplacement("編集済み文言")
        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()
        rule.runOnIdle { saved = "編集済み文言" }
        rule.waitForIdle()
        rule.onNode(editableTextField).assertEditableText("既定の文言")
        rule.runOnIdle { saved = "既定の文言" }
        rule.waitForIdle()
        rule.runOnIdle { saved = "保存後の変更" }
        rule.waitForIdle()

        rule.onNode(editableTextField).assertEditableText("保存後の変更")
    }

    @Test
    fun `前後に空白を含む入力はtrim後の保存値で待機を解除し以降の更新に追従する`() {
        var saved by mutableStateOf("")
        setContent(savedTextProvider = { saved })

        rule.onNode(editableTextField).performTextReplacement(" あい ")
        rule.runOnIdle { saved = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { saved = "外部更新" }
        rule.waitForIdle()

        rule.onNode(editableTextField and hasText("外部更新")).assertExists()
    }

    @Test
    fun `最大長超過入力は制限後の保存値で待機を解除し以降の更新に追従する`() {
        var saved by mutableStateOf("")
        setContent(maxLength = 30, savedTextProvider = { saved })

        rule.onNode(editableTextField).performTextReplacement("あ".repeat(31))
        rule.runOnIdle { saved = "あ".repeat(30) }
        rule.waitForIdle()
        rule.runOnIdle { saved = "外部更新" }
        rule.waitForIdle()

        rule.onNode(editableTextField and hasText("外部更新")).assertExists()
    }

    @Test
    fun `文言の中間のカーソル位置にプレースホルダーを挿入して通知する`() {
        val changedTexts = mutableListOf<String>()
        setContent(onTextChanged = { changedTexts.add(it) })

        rule.onNode(editableTextField).performTextReplacement("前半後半")
        rule.onNode(editableTextField).performTextInputSelection(TextRange(2))
        rule.onNodeWithText("ラップタイムを挿入").performClick()
        rule.waitForIdle()

        assertEquals(listOf("前半後半", "前半{laptime}後半"), changedTexts)
    }

    @Test
    fun `TTS不可では入力と試聴とチップがすべて無効になる`() {
        setContent(isTextToSpeechAvailable = false)

        rule.onNode(editableTextField).assertIsNotEnabled()
        rule.onNodeWithContentDescription("試聴").assertIsNotEnabled()
        rule.onNodeWithText("ラップタイムを挿入").assertIsNotEnabled()
    }

    private fun SemanticsNodeInteraction.assertEditableText(expected: String) {
        assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(expected)))
    }

    private fun setContent(
        savedText: String = "注意",
        savedTextProvider: (() -> String)? = null,
        maxLength: Int = 30,
        isTextToSpeechAvailable: Boolean = true,
        onTextChanged: (String) -> Unit = {},
        onPreviewClick: (String) -> Unit = {},
        unknownPlaceholdersText: @Composable (String) -> String? = { null },
    ) {
        rule.setContent {
            KoDriverTheme {
                PlaceholderReadoutTextField(
                    label = "読み上げ",
                    savedText = savedTextProvider?.invoke() ?: savedText,
                    defaultText = "既定の文言",
                    maxLength = maxLength,
                    isTextToSpeechAvailable = isTextToSpeechAvailable,
                    onTextChanged = onTextChanged,
                    onPreviewClick = onPreviewClick,
                    placeholder = "{laptime}",
                    insertLabel = "ラップタイムを挿入",
                    placeholderHint = "ラップタイムに置き換えます",
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                    unknownPlaceholdersText = unknownPlaceholdersText,
                )
            }
        }
    }
}
