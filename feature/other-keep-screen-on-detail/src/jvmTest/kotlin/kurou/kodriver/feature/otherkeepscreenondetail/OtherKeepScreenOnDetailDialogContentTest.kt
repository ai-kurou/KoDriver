@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherkeepscreenondetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherKeepScreenOnDetailDialogContentTest {

    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherKeepScreenOnDetailUiState = OtherKeepScreenOnDetailUiState(),
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherKeepScreenOnDetailDialogContent(
                uiState = uiState,
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
    fun `ONとOFFのラベルが表示されている`() {
        setContent()

        rule.onNodeWithText("ON").fetchSemanticsNode()
        rule.onNodeWithText("OFF").fetchSemanticsNode()
    }
}
