package kurou.kodriver.feature.otheroverlaytextcolordetail

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherOverlayTextColorDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherOverlayTextColorDetailUiState = OtherOverlayTextColorDetailUiState(),
        onOverlayTextColorSelected: (Long) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherOverlayTextColorDetailDialogContent(
                uiState = uiState,
                onOverlayTextColorSelected = onOverlayTextColorSelected,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
            )
        }
    }

    @Test
    fun `OKボタンをクリックするとonConfirmが呼ばれる`() {
        var confirmCount = 0
        setContent(onConfirm = { confirmCount++ })

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, confirmCount)
    }

    @Test
    fun `キャンセルボタンをクリックするとonDismissが呼ばれる`() {
        var dismissCount = 0
        setContent(onDismiss = { dismissCount++ })

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun `すべての文字色ラベルが表示されている`() {
        setContent()

        listOf("アンバー", "白", "赤", "緑", "青", "シアン", "マゼンタ", "黄").forEach { label ->
            rule.onNodeWithContentDescription(label).fetchSemanticsNode()
        }
        rule.onNodeWithContentDescription("アンバー").assertIsSelected()
    }

    @Test
    fun `文字色をクリックするとonOverlayTextColorSelectedが呼ばれる`() {
        var selectedOverlayTextColor: Long? = null
        setContent(onOverlayTextColorSelected = { selectedOverlayTextColor = it })

        val colorsByLabel =
            listOf(
                "アンバー" to OVERLAY_TEXT_COLOR_DEFAULT,
                "白" to 0xFFFFFFFFL,
                "赤" to 0xFFFF0000L,
                "緑" to 0xFF00FF00L,
                "青" to 0xFF0000FFL,
                "シアン" to 0xFF00FFFFL,
                "マゼンタ" to 0xFFFF00FFL,
                "黄" to 0xFFFFFF00L,
            )
        colorsByLabel.forEach { (label, overlayTextColor) ->
            rule.onNodeWithContentDescription(label).performScrollTo().performClick()

            assertEquals(overlayTextColor, selectedOverlayTextColor)
        }
    }

    @Test
    fun `リセットで既定色を選択する`() {
        var selected: Long? = null
        setContent(
            uiState = OtherOverlayTextColorDetailUiState(pendingOverlayTextColor = 0xFFFFFFFFL),
            onOverlayTextColorSelected = { selected = it },
        )
        rule.onNodeWithContentDescription("白").assertIsSelected()
        rule.onNodeWithText("リセット").performScrollTo().performClick()
        assertEquals(OVERLAY_TEXT_COLOR_DEFAULT, selected)
    }

    @Test
    fun `プリセット以外の保存色では全スウォッチが未選択になる`() {
        setContent(uiState = OtherOverlayTextColorDetailUiState(pendingOverlayTextColor = 0x80123ABCL))
        listOf("アンバー", "白", "赤", "緑", "青", "シアン", "マゼンタ", "黄").forEach { label ->
            rule.onNodeWithContentDescription(label).assertIsNotSelected()
        }
    }
}
