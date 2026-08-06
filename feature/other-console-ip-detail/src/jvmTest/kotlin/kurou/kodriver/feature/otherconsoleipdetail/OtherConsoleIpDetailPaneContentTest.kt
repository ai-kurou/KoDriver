@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class OtherConsoleIpDetailPaneContentTest {
    private data class ContentParams(
        val uiState: OtherConsoleIpDetailUiState =
            OtherConsoleIpDetailUiState(
                inputAddress = "192.168.1.1",
                isInputValid = true,
            ),
        val onPortSelected: (Int) -> Unit = {},
        val onSave: () -> Unit = {},
        val onDismiss: () -> Unit = {},
        val onBack: () -> Unit = {},
        val onOpenGuide: () -> Unit = {},
    )

    private fun DesktopComposeUiTest.setContent(params: ContentParams = ContentParams()) {
        setContent {
            OtherConsoleIpDetailPaneContent(
                uiState = params.uiState,
                onPortSelected = params.onPortSelected,
                onSave = params.onSave,
                onDismiss = params.onDismiss,
                onBack = params.onBack,
                onOpenGuide = params.onOpenGuide,
            )
        }
    }

    @Test
    fun `保存ボタンをクリックするとonSaveが呼ばれる`() =
        composeScreenshotTest {
            var saveCount = 0
            setContent(ContentParams(onSave = { saveCount++ }))

            onNodeWithText("保存").performClick()

            assertEquals(1, saveCount)
        }

    @Test
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() =
        composeScreenshotTest {
            var dismissCount = 0
            var backCount = 0
            setContent(
                ContentParams(
                    uiState = OtherConsoleIpDetailUiState(isSaved = true),
                    onDismiss = { dismissCount++ },
                    onBack = { backCount++ },
                ),
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
                ContentParams(
                    onDismiss = { dismissCount++ },
                    onBack = { backCount++ },
                ),
            )

            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, dismissCount)
            assertEquals(1, backCount)
        }

    @Test
    fun `接続設定ガイドリンクをクリックするとonOpenGuideが呼ばれる`() =
        composeScreenshotTest {
            var guideCount = 0
            setContent(ContentParams(onOpenGuide = { guideCount++ }))

            onNodeWithText("接続設定ガイドを開く").performClick()

            assertEquals(1, guideCount)
        }

    @Test
    fun `33740のラジオボタンをクリックするとonPortSelected(33740)が呼ばれる`() =
        composeScreenshotTest {
            var selected: Int? = null
            setContent(
                ContentParams(
                    uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1", selectedPort = 33741),
                    onPortSelected = { selected = it },
                ),
            )

            onNodeWithText("33740（ゲーム機に直接接続）").performClick()

            assertEquals(33740, selected)
        }

    @Test
    fun `33741のラジオボタンをクリックするとonPortSelected(33741)が呼ばれる`() =
        composeScreenshotTest {
            var selected: Int? = null
            setContent(
                ContentParams(
                    uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1", selectedPort = 33740),
                    onPortSelected = { selected = it },
                ),
            )

            onNodeWithText("33741（SimHub経由で接続）").performClick()

            assertEquals(33741, selected)
        }
}
