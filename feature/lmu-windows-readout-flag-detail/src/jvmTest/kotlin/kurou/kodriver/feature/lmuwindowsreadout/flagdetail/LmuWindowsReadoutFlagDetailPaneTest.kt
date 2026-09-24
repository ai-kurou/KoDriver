package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kurou.kodriver.domain.model.RedFlagVoiceType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutFlagDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `フラッグカードをタップするとonFlagEnabledChangedが呼ばれる`() {
        var changedItem: FlagReadoutItem? = null
        var changedEnabled: Boolean? = null
        rule.setContent {
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
                    onSectorYellowFlagTextChanged = {},
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed().performClick()

        assertEquals(FlagReadoutItem.BlueFlag, changedItem)
        assertEquals(false, changedEnabled)
    }

    @Test
    fun `レッドフラッグカードをタップするとonRedFlagEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = { changedEnabled = it },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onSectorYellowFlagTextChanged = {},
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[0].performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `レッドフラッグチップをタップするとonRedFlagVoiceTypeChangedとonRedFlagPreviewClickedが呼ばれる`() {
        var changedVoiceType: RedFlagVoiceType? = null
        var previewedVoiceType: RedFlagVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onSectorYellowFlagTextChanged = {},
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[1].performClick()

        assertEquals(RedFlagVoiceType.RED_FLAG, changedVoiceType)
        assertEquals(RedFlagVoiceType.RED_FLAG, previewedVoiceType)
    }

    @Test
    fun `セッションストップチップをタップするとonRedFlagVoiceTypeChangedとonRedFlagPreviewClickedが呼ばれる`() {
        var changedVoiceType: RedFlagVoiceType? = null
        var previewedVoiceType: RedFlagVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(redFlagVoiceType = RedFlagVoiceType.RED_FLAG),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onSectorYellowFlagTextChanged = {},
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("セッションストップ")[0].performClick()

        assertEquals(RedFlagVoiceType.SESSION_STOP, changedVoiceType)
        assertEquals(RedFlagVoiceType.SESSION_STOP, previewedVoiceType)
    }

    @Test
    fun `イエローフラッグのカスタム文言を入力するとonSectorYellowFlagTextChangedが呼ばれる`() {
        var changedText: String? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onSectorYellowFlagTextChanged = { changedText = it },
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[2].performTextInput("イエロー、注意")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()

        assertEquals("イエロー、注意", changedText)
    }

    @Test
    fun `TTSを利用できない場合はカスタム文言を入力できない`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = false),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onSectorYellowFlagTextChanged = {},
                    onSectorYellowFlagTextPreviewClicked = {},
                )
            }
        }

        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
    }
}
