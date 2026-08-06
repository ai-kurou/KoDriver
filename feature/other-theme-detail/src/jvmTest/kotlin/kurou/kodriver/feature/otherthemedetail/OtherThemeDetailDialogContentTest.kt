@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherthemedetail

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Test
import kotlin.test.assertEquals

class OtherThemeDetailDialogContentTest {
    private fun DesktopComposeUiTest.setContent(
        uiState: OtherThemeDetailUiState = OtherThemeDetailUiState(),
        onThemeModeSelected: (ThemeMode) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        setContent {
            OtherThemeDetailDialogContent(
                uiState = uiState,
                onThemeModeSelected = onThemeModeSelected,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
            )
        }
    }

    @Test
    fun `OKボタンをクリックするとonConfirmが呼ばれる`() =
        composeScreenshotTest {
            var confirmCount = 0
            setContent(onConfirm = { confirmCount++ })

            onNodeWithText("OK").performClick()

            assertEquals(1, confirmCount)
        }

    @Test
    fun `キャンセルボタンをクリックするとonDismissが呼ばれる`() =
        composeScreenshotTest {
            var dismissCount = 0
            setContent(onDismiss = { dismissCount++ })

            onNodeWithText("キャンセル").performClick()

            assertEquals(1, dismissCount)
        }

    @Test
    fun `すべてのテーマモードラベルが表示されている`() =
        composeScreenshotTest {
            setContent()

            onNodeWithText("システムに従う").fetchSemanticsNode()
            onNodeWithText("ライト").fetchSemanticsNode()
            onNodeWithText("ダーク").fetchSemanticsNode()
        }

    @Test
    fun `テーマモードをクリックするとonThemeModeSelectedが呼ばれる`() =
        composeScreenshotTest {
            var selectedThemeMode: ThemeMode? = null
            setContent(onThemeModeSelected = { selectedThemeMode = it })

            onNodeWithText("ダーク").performClick()

            assertEquals(ThemeMode.DARK, selectedThemeMode)
        }
}
