@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class OtherReadoutStartSoundDetailDialogContentTest {
    private fun DesktopComposeUiTest.setContent(
        uiState: OtherReadoutStartSoundDetailUiState = OtherReadoutStartSoundDetailUiState(),
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        setContent {
            OtherReadoutStartSoundDetailDialogContent(
                uiState = uiState,
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
    fun `すべての種別ラベルが表示されている`() =
        composeScreenshotTest {
            setContent()

            onNodeWithText("電子ノイズ").fetchSemanticsNode()
            onNodeWithText("Formula無線").fetchSemanticsNode()
        }
}
