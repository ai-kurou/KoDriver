package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DetailPaneLabeledTextFieldTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ラベルとプレースホルダーと補足文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneLabeledTextField(
                    label = "左側の読み上げ",
                    value = "",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                    supportingText = "空欄のままなら読み上げません",
                )
            }
        }

        // ラベルとプレースホルダーの2か所に同じ文言が表示される
        rule.onAllNodes(hasText("左側の読み上げ")).assertCountEquals(2)
        rule.onNodeWithText("空欄のままなら読み上げません").assertIsDisplayed()
    }

    @Test
    fun `ラベルとTextFieldの間隔はextraSmallになる`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneLabeledTextField(
                    label = "左側の読み上げ",
                    value = "",
                    maxLength = 30,
                    onValueChangeFinished = {},
                    onPreviewClick = {},
                )
            }
        }

        val labelBounds =
            rule
                .onAllNodes(hasText("左側の読み上げ"))[0]
                .fetchSemanticsNode()
                .boundsInRoot
        val fieldBounds = rule.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        assertEquals(expected, fieldBounds.top - labelBounds.bottom, absoluteTolerance = 1f)
    }

    @Test
    fun `入力と試聴がDetailPaneCardTextFieldに委譲される`() {
        var finishedText: String? = null
        var previewText: String? = null
        rule.setContent {
            KoDriverTheme {
                DetailPaneLabeledTextField(
                    label = "左側の読み上げ",
                    value = "",
                    maxLength = 30,
                    onValueChangeFinished = { finishedText = it },
                    onPreviewClick = { previewText = it },
                    previewContentDescription = "試聴",
                )
            }
        }

        rule.onNode(hasSetTextAction()).performTextInput("左")
        rule.onNodeWithContentDescription("試聴").performClick()

        assertEquals("左", finishedText)
        assertEquals("左", previewText)
    }
}
