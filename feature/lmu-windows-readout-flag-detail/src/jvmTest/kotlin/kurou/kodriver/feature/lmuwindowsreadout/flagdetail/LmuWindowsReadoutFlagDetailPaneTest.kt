package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
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
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[0].assertIsDisplayed().performClick()

        assertEquals(FlagReadoutItem.BlueFlag, changedItem)
        assertEquals(false, changedEnabled)
    }

    @Test
    fun `レッドフラッグカードをタップするとレッドフラッグでonFlagEnabledChangedが呼ばれる`() {
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
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[0].performScrollTo().performClick()

        assertEquals(FlagReadoutItem.RedFlag, changedItem)
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
                    onRecordedVoiceSelected = { _, preview -> preview() },
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
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
                    onRecordedVoiceSelected = { _, preview -> preview() },
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
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
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, text -> changedText = text },
                    onFlagTextPreviewClicked = { _, _ -> },
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
                            flagTexts = mapOf(FlagReadoutItem.BlueFlag to "", FlagReadoutItem.FullCourseYellow to "減速"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { item, text -> changes += item to text },
                    onFlagTextPreviewClicked = { item, text -> previews += item to text },
                )
            }
        }

        rule.onAllNodesWithText("ブルーフラッグ")[1].performTextInput("譲って")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performClick()

        assertEquals(listOf(FlagReadoutItem.BlueFlag to "譲って"), changes)
        assertEquals(
            listOf(FlagReadoutItem.BlueFlag to "譲って", FlagReadoutItem.FullCourseYellow to "減速"),
            previews,
        )
    }

    @Test
    fun `ブルーフラッグは収録音声のチップを表示せず既定の文言が入力欄に表示される`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        // カードのタイトルと入力欄の既定文言のみ（チップがあれば3つになる）。
        rule.onAllNodesWithText("ブルーフラッグ").assertCountEquals(2)
        // 他のフラッグはタイトル・チップ・入力欄（プレースホルダー）の3つ。
        rule.onAllNodesWithText("イエローフラッグ").assertCountEquals(3)
    }

    @Test
    fun `TTSを利用できない場合はカスタム文言を入力できない`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = false),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
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
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithText("この文言を音声合成で読み上げます（収録音声は使いません）")[0].assertIsDisplayed()
        rule.onAllNodesWithText("イエローフラッグ")[1].assertIsNotSelected()
    }

    @Test
    fun `カスタム文言があるときにイエローフラッグのチップをタップすると文言は消さず収録音声が選ばれる`() {
        var changedText: String? = null
        var previewedItem: FlagReadoutItem? = null
        var recordedVoiceItem: FlagReadoutItem? = null
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
                    onRecordedVoiceSelected = { item, preview ->
                        recordedVoiceItem = item
                        preview()
                    },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, text -> changedText = text },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[1].performClick()

        assertEquals(null, changedText)
        assertEquals(FlagReadoutItem.SectorYellowFlag, recordedVoiceItem)
        assertEquals(FlagReadoutItem.SectorYellowFlag, previewedItem)
    }

    @Test
    fun `文言が残っていても収録音声が選ばれているときはチップが選択状態になり文言保持の案内を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.SectorYellowFlag to "イエロー、前方注意"),
                            recordedVoiceSelected = mapOf(FlagReadoutItem.SectorYellowFlag to true),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[1].assertIsSelected()
        rule
            .onAllNodesWithText("収録音声で読み上げます（入力した文言は保持。編集するとこの文言に切り替わります）")[0]
            .assertIsDisplayed()
    }

    @Test
    fun `カスタム文言を1文字入力しただけで確定操作なしに入力欄が選択状態になる`() {
        var uiState by mutableStateOf(LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true))
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = uiState,
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { item, text ->
                        uiState = uiState.copy(flagTexts = mapOf(item to text))
                    },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[2].performTextInput("イ")

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithText("イエローフラッグ")[1].assertIsNotSelected()
    }

    @Test
    fun `レッドフラッグのカスタム文言を入力するとレッドフラッグでonFlagTextChangedとonFlagTextPreviewClickedが呼ばれる`() {
        val changes = mutableListOf<Pair<FlagReadoutItem, String>>()
        val previews = mutableListOf<Pair<FlagReadoutItem, String>>()
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { item, text -> changes += item to text },
                    onFlagTextPreviewClicked = { item, text -> previews += item to text },
                )
            }
        }

        rule.onAllNodesWithText("セッションストップ")[1].performScrollTo().performTextInput("赤旗、停止")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].performScrollTo().performClick()

        assertEquals(listOf(FlagReadoutItem.RedFlag to "赤旗、停止"), changes)
        assertEquals(listOf(FlagReadoutItem.RedFlag to "赤旗、停止"), previews)
    }

    @Test
    fun `レッドフラッグのカスタム文言があるときは入力欄が選択状態になりチップの選択は外れる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.RedFlag to "赤旗、停止"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { _, _ -> },
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("セッションストップ")[0].performScrollTo().assertIsNotSelected()
    }

    @Test
    fun `カスタム文言があるときにレッドフラッグのチップをタップすると文言は消さず収録音声と音声種別が選ばれる`() {
        var changed: Pair<FlagReadoutItem, String>? = null
        var recordedVoiceItem: FlagReadoutItem? = null
        var changedVoiceType: RedFlagVoiceType? = null
        var previewedVoiceType: RedFlagVoiceType? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.RedFlag to "赤旗、停止"),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRecordedVoiceSelected = { item, preview ->
                        recordedVoiceItem = item
                        preview()
                    },
                    onRedFlagVoiceTypeChanged = { changedVoiceType = it },
                    onRedFlagPreviewClicked = { previewedVoiceType = it },
                    onFlagTextChanged = { item, text -> changed = item to text },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("セッションストップ")[0].performScrollTo().performClick()

        assertEquals(null, changed)
        assertEquals(FlagReadoutItem.RedFlag, recordedVoiceItem)
        assertEquals(RedFlagVoiceType.SESSION_STOP, changedVoiceType)
        assertEquals(RedFlagVoiceType.SESSION_STOP, previewedVoiceType)
    }
}
