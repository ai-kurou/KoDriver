@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDetailPaneContentTest {
    private fun DesktopComposeUiTest.setContent(
        uiState: OtherServerIpDetailUiState = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true),
        onSave: () -> Unit = {},
        onSaveAnyway: () -> Unit = {},
        onDismiss: () -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        setContent {
            OtherServerIpDetailPaneContent(
                uiState = uiState,
                onSave = onSave,
                onSaveAnyway = onSaveAnyway,
                onDismiss = onDismiss,
                onBack = onBack,
            )
        }
    }

    @Test
    fun `保存ボタンをクリックするとonSaveが呼ばれる`() =
        composeScreenshotTest {
            var saveCount = 0
            setContent(onSave = { saveCount++ })

            onNodeWithText("保存").performClick()

            assertEquals(1, saveCount)
        }

    @Test
    fun `このまま保存ボタンをクリックするとonSaveAnywayが呼ばれる`() =
        composeScreenshotTest {
            var saveAnywayCount = 0
            setContent(
                uiState =
                    OtherServerIpDetailUiState(
                        inputIp = "192.168.1.1",
                        isInputValid = true,
                        connectivityWarning = true,
                    ),
                onSaveAnyway = { saveAnywayCount++ },
            )

            onNodeWithText("このまま保存").performClick()

            assertEquals(1, saveAnywayCount)
        }

    @Test
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() =
        composeScreenshotTest {
            var dismissCount = 0
            var backCount = 0
            setContent(
                uiState = OtherServerIpDetailUiState(isSaved = true),
                onDismiss = { dismissCount++ },
                onBack = { backCount++ },
            )

            waitForIdle()

            assertEquals(1, dismissCount)
            assertEquals(1, backCount)
        }

    @Test
    fun `戻るボタンをクリックするとonDismissとonBackが呼ばれる`() =
        composeScreenshotTest {
            var dismissCount = 0
            var backCount = 0
            setContent(
                onDismiss = { dismissCount++ },
                onBack = { backCount++ },
            )

            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, dismissCount)
            assertEquals(1, backCount)
        }
}
