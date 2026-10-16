package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ReadoutTextFieldTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `TTS不可では未知プレースホルダより不可の説明を優先する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "注意",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = false,
                    onTextChanged = {},
                    onPreviewClick = {},
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                    unknownPlaceholdersText = "未知の文言",
                )
            }
        }

        rule.onNodeWithText("読み上げ不可").assertIsDisplayed()
        rule.onNodeWithText("未知の文言").assertDoesNotExist()
        rule.onNodeWithText("空欄の説明").assertDoesNotExist()
        rule.onNodeWithText("注意").assertIsNotEnabled()
        rule.onNodeWithContentDescription("試聴").assertIsNotEnabled()
        rule.onNodeWithContentDescription("選択中").assertIsDisplayed()
    }

    @Test
    fun `未知プレースホルダの説明を入力済みでも表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "注意",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = true,
                    onTextChanged = {},
                    onPreviewClick = {},
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                    unknownPlaceholdersText = "未知の文言",
                )
            }
        }

        rule.onNodeWithText("未知の文言").assertIsDisplayed()
        rule.onNodeWithText("空欄の説明").assertDoesNotExist()
        rule.onNodeWithText("読み上げ不可").assertDoesNotExist()
        rule.onNodeWithContentDescription("選択中").assertIsDisplayed()
    }

    @Test
    fun `入力済みで未知プレースホルダ文言が空なら説明を表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "注意",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = true,
                    onTextChanged = {},
                    onPreviewClick = {},
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                    unknownPlaceholdersText = "",
                )
            }
        }

        rule.onNodeWithText("空欄の説明").assertDoesNotExist()
        rule.onNodeWithText("読み上げ不可").assertDoesNotExist()
        rule.onNodeWithContentDescription("選択中").assertIsDisplayed()
    }

    @Test
    fun `空白のみで未知プレースホルダ文言がなければ既定説明を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "  ",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = true,
                    onTextChanged = {},
                    onPreviewClick = {},
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                )
            }
        }

        rule.onNodeWithText("空欄の説明").assertIsDisplayed()
        rule.onNodeWithContentDescription("選択中").assertDoesNotExist()
    }

    @Test
    fun `リセットで既定文言を一度通知する`() {
        val changedTexts = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "注意",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = true,
                    onTextChanged = { changedTexts.add(it) },
                    onPreviewClick = {},
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()

        assertEquals(listOf("既定の文言"), changedTexts)
    }

    @Test
    fun `編集と試聴は入力中の文言を一度通知する`() {
        val changedTexts = mutableListOf<String>()
        val previewTexts = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ReadoutTextField(
                    label = "読み上げ",
                    text = "注意",
                    defaultText = "既定の文言",
                    maxLength = 30,
                    isTextToSpeechAvailable = true,
                    onTextChanged = { changedTexts.add(it) },
                    onPreviewClick = { previewTexts.add(it) },
                    insertState = rememberTextInsertState(),
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                )
            }
        }

        rule.onNode(hasSetTextAction()).performTextReplacement("編集した文言")
        rule.onNodeWithContentDescription("試聴").performClick()

        assertEquals(listOf("編集した文言"), changedTexts)
        assertEquals(listOf("編集した文言"), previewTexts)
    }
}
