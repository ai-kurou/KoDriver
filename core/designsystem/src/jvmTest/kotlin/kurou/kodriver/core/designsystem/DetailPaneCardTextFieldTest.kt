package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun `マウント直後は入力していないため確定されない`() {
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
    fun `1文字入力するたびにonValueChangeFinishedが呼ばれて即座に確定される`() {
        var finishedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = { finishedText = it },
                    onPreviewClick = {},
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イ")

        assertEquals("イ", finishedText)
    }

    @Test
    fun `再生ボタンを押すと入力中の文言が試聴される`() {
        var previewedText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = { previewedText = it },
                    previewContentDescription = "入力した文言を再生",
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イエロー、注意")
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()

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
    fun `値が空のときは文字数カウンターが0とmaxLengthで表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                )
            }
        }

        rule.onNodeWithText("0/30").assertIsDisplayed()
    }

    @Test
    fun `入力するたびに文字数カウンターが更新される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                )
            }
        }

        rule.onNodeWithText("イエローフラッグ").performTextInput("イエロー")

        rule.onNodeWithText("4/30").assertIsDisplayed()
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
    fun `補足文言がなくても文字数カウンターは右端に表示される`() {
        assertCounterIsRightAligned(supportingText = null)
    }

    @Test
    fun `長い補足文言でも文字数カウンターの領域を確保して右端に表示される`() {
        assertCounterIsRightAligned(supportingText = "空欄のままなら収録音声で読み上げます".repeat(5))
    }

    @Test
    fun `編集中の文言が既定値と異なるときだけリセットできる`() {
        var resetCount = 0
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "イエローフラッグ",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    defaultValue = "イエローフラッグ",
                    onResetToDefault = { resetCount++ },
                    resetContentDescription = "デフォルトに戻す",
                )
            }
        }

        val reset = rule.onNodeWithContentDescription("デフォルトに戻す")
        reset.assertIsNotEnabled().performClick()
        assertEquals(0, resetCount)
        rule.onNodeWithText("イエローフラッグ").performTextReplacement("注意")
        reset.assertIsEnabled().assertIsDisplayed().performClick()
        assertEquals(1, resetCount)
        rule.onNodeWithText("注意").performTextReplacement("イエローフラッグ")
        reset.assertIsNotEnabled().performClick()
        assertEquals(1, resetCount)
    }

    @Test
    fun `enabledがfalseのときは既定値と異なってもリセットできない`() {
        var resetCount = 0
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "注意",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    enabled = false,
                    defaultValue = "イエローフラッグ",
                    onResetToDefault = { resetCount++ },
                    resetContentDescription = "デフォルトに戻す",
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled().performClick()
        assertEquals(0, resetCount)
    }

    @Test
    fun `既定値がnullのときはリセットできない`() {
        var resetCount = 0
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "注意",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    onResetToDefault = { resetCount++ },
                    resetContentDescription = "デフォルトに戻す",
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").assertIsNotEnabled().performClick()
        assertEquals(0, resetCount)
    }

    @Test
    fun `リセットコールバックがないときはボタンを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneCardTextField(
                    value = "注意",
                    placeholder = "イエローフラッグ",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    defaultValue = "イエローフラッグ",
                    resetContentDescription = "デフォルトに戻す",
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").assertDoesNotExist()
    }

    private fun assertCounterIsRightAligned(supportingText: String?) {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.width(360.dp)) {
                    DetailPaneCardTextField(
                        value = "",
                        placeholder = "イエローフラッグ",
                        maxLength = 30,
                        onValueChangeFinished = {},
                        onPreviewClick = {},
                        supportingText = supportingText,
                        modifier = Modifier.testTag("textField"),
                    )
                }
            }
        }

        val counter = rule.onNodeWithText("0/30", useUnmergedTree = true)
        counter.assertIsDisplayed()
        val layoutResults = mutableListOf<TextLayoutResult>()
        counter.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layoutResults) }
        assertEquals(1, layoutResults.single().lineCount)
        val counterBounds = counter.getUnclippedBoundsInRoot()
        val fieldBounds = rule.onNodeWithTag("textField").getUnclippedBoundsInRoot()
        assertEquals(fieldBounds.right - 16.dp, counterBounds.right)
        if (supportingText != null) {
            val supportingBounds =
                rule.onNodeWithText(supportingText, useUnmergedTree = true).getUnclippedBoundsInRoot()
            assertTrue(supportingBounds.right <= counterBounds.left)
            assertTrue(supportingBounds.bottom - supportingBounds.top > counterBounds.bottom - counterBounds.top)
        }
    }
}
