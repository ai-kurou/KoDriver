package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
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
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
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
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[1].performScrollTo().performClick()

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
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("セッションストップ")[0].performScrollTo().performClick()

        assertEquals(RedFlagVoiceType.SESSION_STOP, changedVoiceType)
        assertEquals(RedFlagVoiceType.SESSION_STOP, previewedVoiceType)
    }

    @Test
    fun `イエローフラッグのカスタム文言を入力するとonFlagTextChangedが呼ばれる`() {
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
                    onFlagTextChanged = { _, text -> changedText = text },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[2].performTextInput("イエロー、注意")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()

        assertEquals("イエロー、注意", changedText)
    }

    @Test
    fun `ブルーフラッグとフルコースイエローのカスタム文言を入力すると対応する項目でonFlagTextChangedが呼ばれる`() {
        val changes = mutableListOf<Pair<FlagReadoutItem, String>>()
        val previews = mutableListOf<Pair<FlagReadoutItem, String>>()
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.FullCourseYellow to "減速"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { item, text -> changes += item to text },
                    onFlagTextPreviewClicked = { item, text -> previews += item to text },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[2].performTextInput("譲って")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performClick()

        assertEquals(listOf(FlagReadoutItem.BlueFlag to "譲って"), changes)
        assertEquals(
            listOf(FlagReadoutItem.BlueFlag to "譲って", FlagReadoutItem.FullCourseYellow to "減速"),
            previews,
        )
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
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
    }

    @Test
    fun `カスタム文言があるときは入力欄が選択状態になりチップの選択は外れる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.SectorYellowFlag to "イエロー、前方注意"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithText("この文言を音声合成で読み上げます（収録音声は使いません）")[0].assertIsDisplayed()
        rule.onAllNodesWithText("イエローフラッグ")[1].assertIsNotSelected()
    }

    @Test
    fun `カスタム文言があるときにイエローフラッグのチップをタップするとカスタム文言がクリアされる`() {
        var changedText: String? = null
        var previewedItem: FlagReadoutItem? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.SectorYellowFlag to "イエロー、前方注意"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = { previewedItem = it },
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, text -> changedText = text },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[1].performClick()

        assertEquals("", changedText)
        assertEquals(FlagReadoutItem.SectorYellowFlag, previewedItem)
    }

    @Test
    fun `カスタム文言を1文字入力しただけで確定操作なしに入力欄が選択状態になる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[2].performTextInput("イ")

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithText("イエローフラッグ")[1].assertIsNotSelected()
    }

    @Test
    fun `レッドフラッグのカスタム文言を入力すると入力と試聴のコールバックが呼ばれる`() {
        var changedText: String? = null
        var previewed: Pair<String, RedFlagVoiceType>? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            redFlagVoiceType = RedFlagVoiceType.RED_FLAG,
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = { changedText = it },
                    onRedFlagTextPreviewClicked = { text, type -> previewed = text to type },
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[2].performScrollTo().performTextInput("赤旗、停止")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].performScrollTo().performClick()

        assertEquals("赤旗、停止", changedText)
        assertEquals("赤旗、停止" to RedFlagVoiceType.RED_FLAG, previewed)
    }

    @Test
    fun `レッドフラッグのカスタム文言があるときは入力欄が選択状態になりチップの選択は外れる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            redFlagText = "赤旗、停止",
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = {},
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("セッションストップ")[0].performScrollTo().assertIsNotSelected()
    }

    @Test
    fun `カスタム文言があるときにレッドフラッグのチップをタップすると文言がクリアされ音声種別が選ばれる`() {
        var changedText: String? = null
        var changedVoiceType: RedFlagVoiceType? = null
        var previewedVoiceType: RedFlagVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            redFlagText = "赤旗、停止",
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                    onRedFlagTextChanged = { changedText = it },
                    onRedFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("セッションストップ")[0].performScrollTo().performClick()

        assertEquals("", changedText)
        assertEquals(RedFlagVoiceType.SESSION_STOP, changedVoiceType)
        assertEquals(RedFlagVoiceType.SESSION_STOP, previewedVoiceType)
    }
}
