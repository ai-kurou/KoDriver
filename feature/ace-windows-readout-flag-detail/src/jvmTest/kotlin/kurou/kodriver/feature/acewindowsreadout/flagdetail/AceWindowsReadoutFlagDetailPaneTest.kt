package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutFlagDetailPaneTest {
    @Test
    fun `説明文とブルーフラッグカードが表示される`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    AceWindowsReadoutFlagDetailPaneContent()
                }
            }

            onNodeWithText(
                "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
            ).assertIsDisplayed()
            onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed()
        }

    @Test
    fun `フラッグカードをタップするとonFlagEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedItem: FlagReadoutItem? = null
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme {
                    AceWindowsReadoutFlagDetailPaneContent(
                        uiState = AceWindowsReadoutFlagDetailUiState(),
                        onFlagEnabledChanged = { item, enabled ->
                            changedItem = item
                            changedEnabled = enabled
                        },
                        onPreviewClicked = {},
                    )
                }
            }

            onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed().performClick()

            assertEquals(FlagReadoutItem.BlueFlag, changedItem)
            assertEquals(false, changedEnabled)
        }

    @Test
    fun `フラッグチップをタップするとonPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var previewedItem: FlagReadoutItem? = null
            setContent {
                MaterialTheme {
                    AceWindowsReadoutFlagDetailPaneContent(
                        uiState = AceWindowsReadoutFlagDetailUiState(),
                        onFlagEnabledChanged = { _, _ -> },
                        onPreviewClicked = { previewedItem = it },
                    )
                }
            }

            onAllNodesWithText("ブルーフラッグ")[1].performClick()

            assertEquals(FlagReadoutItem.BlueFlag, previewedItem)
        }
}
