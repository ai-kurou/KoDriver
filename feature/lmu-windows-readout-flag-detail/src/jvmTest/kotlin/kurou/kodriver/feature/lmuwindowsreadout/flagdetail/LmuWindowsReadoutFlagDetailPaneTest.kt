package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.RedFlagVoiceType
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutFlagDetailPaneTest {
    @Test
    fun `フラッグカードをタップするとonFlagEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedItem: FlagReadoutItem? = null
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutFlagDetailPaneContent(
                        uiState = LmuWindowsReadoutFlagDetailUiState(),
                        onFlagEnabledChanged = { item, enabled ->
                            changedItem = item
                            changedEnabled = enabled
                        },
                        onPreviewClicked = {},
                        onRedFlagEnabledChanged = {},
                        onRedFlagVoiceTypeChanged = {},
                        onRedFlagPreviewClicked = {},
                    )
                }
            }

            onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed().performClick()

            assertEquals(FlagReadoutItem.BlueFlag, changedItem)
            assertEquals(false, changedEnabled)
        }

    @Test
    fun `レッドフラッグカードをタップするとonRedFlagEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutFlagDetailPaneContent(
                        uiState = LmuWindowsReadoutFlagDetailUiState(),
                        onFlagEnabledChanged = { _, _ -> },
                        onPreviewClicked = {},
                        onRedFlagEnabledChanged = { changedEnabled = it },
                        onRedFlagVoiceTypeChanged = {},
                        onRedFlagPreviewClicked = {},
                    )
                }
            }

            onAllNodesWithText("レッドフラッグ")[0].performClick()

            assertEquals(false, changedEnabled)
        }

    @Test
    fun `レッドフラッグチップをタップするとonRedFlagVoiceTypeChangedとonRedFlagPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var changedVoiceType: RedFlagVoiceType? = null
            var previewedVoiceType: RedFlagVoiceType? = null
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutFlagDetailPaneContent(
                        uiState = LmuWindowsReadoutFlagDetailUiState(),
                        onFlagEnabledChanged = { _, _ -> },
                        onPreviewClicked = {},
                        onRedFlagEnabledChanged = {},
                        onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                        onRedFlagPreviewClicked = { previewedVoiceType = it },
                    )
                }
            }

            onAllNodesWithText("レッドフラッグ")[1].performClick()

            assertEquals(RedFlagVoiceType.RED_FLAG, changedVoiceType)
            assertEquals(RedFlagVoiceType.RED_FLAG, previewedVoiceType)
        }

    @Test
    fun `セッションストップチップをタップするとonRedFlagVoiceTypeChangedとonRedFlagPreviewClickedが呼ばれる`() =
        composeScreenshotTest {
            var changedVoiceType: RedFlagVoiceType? = null
            var previewedVoiceType: RedFlagVoiceType? = null
            setContent {
                MaterialTheme {
                    LmuWindowsReadoutFlagDetailPaneContent(
                        uiState = LmuWindowsReadoutFlagDetailUiState(redFlagVoiceType = RedFlagVoiceType.RED_FLAG),
                        onFlagEnabledChanged = { _, _ -> },
                        onPreviewClicked = {},
                        onRedFlagEnabledChanged = {},
                        onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                        onRedFlagPreviewClicked = { previewedVoiceType = it },
                    )
                }
            }

            onAllNodesWithText("セッションストップ")[0].performClick()

            assertEquals(RedFlagVoiceType.SESSION_STOP, changedVoiceType)
            assertEquals(RedFlagVoiceType.SESSION_STOP, previewedVoiceType)
        }
}
