@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherthemedetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherThemeDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherThemeDetailUiState = OtherThemeDetailUiState(),
        onThemeModeSelected: (ThemeMode) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherThemeDetailDialogContent(
                uiState = uiState,
                onThemeModeSelected = onThemeModeSelected,
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
    fun `すべてのテーマモードラベルが表示されている`() {
        setContent()

        rule.onNodeWithText("システムに従う").fetchSemanticsNode()
        rule.onNodeWithText("ライト").fetchSemanticsNode()
        rule.onNodeWithText("ダーク").fetchSemanticsNode()
    }

    @Test
    fun `テーマモードをクリックするとonThemeModeSelectedが呼ばれる`() {
        var selectedThemeMode: ThemeMode? = null
        setContent(onThemeModeSelected = { selectedThemeMode = it })

        rule.onNodeWithText("ダーク").performClick()

        assertEquals(ThemeMode.DARK, selectedThemeMode)
    }
}
