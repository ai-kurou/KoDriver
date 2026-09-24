package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailPaneCardTextFieldTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `値が空のときはプレースホルダーが表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    supportingText = "空欄のままなら収録音声で読み上げます",
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").assertIsDisplayed()
        rule.onNodeWithText("空欄のままなら収録音声で読み上げます").assertIsDisplayed()
    }

    @Test
    fun `再生ボタンを押すと入力中の文言が確定され試聴される`() {
        var finishedText: String? = null
        var previewedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = { finishedText = it },
                    onPreviewClick = { previewedText = it },
                    previewContentDescription = "入力した文言を再生",
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イエロー、注意")
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()

        assertEquals("イエロー、注意", finishedText)
        assertEquals("イエロー、注意", previewedText)
    }

    @Test
    fun `最大文字数を超える入力は切り捨てられる`() {
        var previewedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 5,
                    onValueChangeFinished = {},
                    onPreviewClick = { previewedText = it },
                    previewContentDescription = "入力した文言を再生",
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("あいうえおかきくけこ")
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()

        assertEquals("あいうえお", previewedText)
    }

    @Test
    fun `enabledがfalseのときは入力も再生もできない`() {
        var previewedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = { previewedText = it },
                    enabled = false,
                    previewContentDescription = "入力した文言を再生",
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").assertIsNotEnabled()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsNotEnabled()

        assertNull(previewedText)
    }
}
