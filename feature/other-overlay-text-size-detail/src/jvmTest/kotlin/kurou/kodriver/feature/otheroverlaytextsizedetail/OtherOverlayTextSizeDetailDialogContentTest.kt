package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.domain.model.OverlayTextSize
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherOverlayTextSizeDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherOverlayTextSizeDetailUiState = OtherOverlayTextSizeDetailUiState(),
        onOverlayTextSizeSelected: (OverlayTextSize) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherOverlayTextSizeDetailDialogContent(
                uiState = uiState,
                onOverlayTextSizeSelected = onOverlayTextSizeSelected,
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
    fun `すべての文字サイズラベルが表示されている`() {
        setContent()

        listOf("極小", "小", "中", "大", "特大", "超特大", "最大").forEach { label ->
            rule.onNodeWithText(label).fetchSemanticsNode()
        }
    }

    @Test
    fun `文字サイズをクリックするとonOverlayTextSizeSelectedが呼ばれる`() {
        var selectedOverlayTextSize: OverlayTextSize? = null
        setContent(onOverlayTextSizeSelected = { selectedOverlayTextSize = it })

        val sizesByLabel =
            listOf(
                "極小" to OverlayTextSize.EXTRA_SMALL,
                "小" to OverlayTextSize.SMALL,
                "中" to OverlayTextSize.MEDIUM,
                "大" to OverlayTextSize.LARGE,
                "特大" to OverlayTextSize.EXTRA_LARGE,
                "超特大" to OverlayTextSize.HUGE,
                "最大" to OverlayTextSize.MAXIMUM,
            )
        sizesByLabel.forEach { (label, overlayTextSize) ->
            rule.onNodeWithText(label).performClick()

            assertEquals(overlayTextSize, selectedOverlayTextSize)
        }
    }
}
