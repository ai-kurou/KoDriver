package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.width
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaceholderReadoutTextFieldLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ヒントは長い文言でもチップの下に全幅で配置する`() {
        val insertLabel = "前方{ahead}秒、後方{behind}秒を挿入"
        val hint = "{ahead}は前の車との差の秒数、{behind}は後ろの車との差の秒数に置き換わります"
        rule.setContent {
            KoDriverTheme {
                PlaceholderReadoutTextField(
                    label = "読み上げ",
                    savedText = "注意",
                    defaultText = "既定の文言",
                    maxLength = 40,
                    isTextToSpeechAvailable = true,
                    onTextChanged = {},
                    onPreviewClick = {},
                    placeholder = "{ahead}",
                    insertLabel = insertLabel,
                    placeholderHint = hint,
                    resetContentDescription = "デフォルトに戻す",
                    previewContentDescription = "試聴",
                    selectedContentDescription = "選択中",
                    unavailableText = "読み上げ不可",
                    supportingText = "空欄の説明",
                )
            }
        }

        val chipBounds = rule.onNodeWithText(insertLabel).getBoundsInRoot()
        val hintBounds = rule.onNodeWithText(hint).getBoundsInRoot()
        val rootBounds = rule.onRoot().getBoundsInRoot()

        assertTrue(hintBounds.top >= chipBounds.bottom)
        assertEquals(rootBounds.width, hintBounds.width)
        assertEquals(chipBounds.left, hintBounds.left)
    }
}
