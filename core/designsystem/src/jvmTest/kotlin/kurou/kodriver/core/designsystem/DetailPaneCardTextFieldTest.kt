package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    fun `マウント直後は一度もフォーカスされていないため確定されない`() {
        var finishedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "イエロー、前方注意",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = { finishedText = it },
                    onPreviewClick = {},
                )
            }
        }
        rule.waitForIdle()

        assertNull(finishedText)
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

    @Test
    fun `selectedがtrueのときはチェックアイコンが表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "イエロー、前方注意",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    selected = true,
                    selectedContentDescription = "この文言を読み上げます",
                )
            }
        }

        rule.onNodeWithContentDescription("この文言を読み上げます").assertIsDisplayed()
    }

    @Test
    fun `selectedがfalseのときはチェックアイコンが表示されない`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    selectedContentDescription = "この文言を読み上げます",
                )
            }
        }

        rule.onNodeWithContentDescription("この文言を読み上げます").assertDoesNotExist()
    }

    @Test
    fun `1文字入力するたびにonTextChangedが呼ばれる`() {
        var changedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    onTextChanged = { changedText = it },
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イ")

        assertEquals("イ", changedText)
    }

    @Test
    fun `フォーカスを外さずに破棄されても入力中の文言が確定される`() {
        var finishedText: String? = null
        lateinit var hide: () -> Unit
        rule.setContent {
            var visible by remember { mutableStateOf(true) }
            hide = { visible = false }
            KoDriverTheme {
                if (visible) {
                    DetailPaneCardTextField(
                        value = "",
                        placeholder = "イエローフラッグ",
                        maxLength = 30,
                        onValueChangeFinished = { finishedText = it },
                        onPreviewClick = {},
                    )
                }
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イエロー、注意")
        rule.runOnIdle { hide() }
        rule.waitForIdle()

        assertEquals("イエロー、注意", finishedText)
    }
}
