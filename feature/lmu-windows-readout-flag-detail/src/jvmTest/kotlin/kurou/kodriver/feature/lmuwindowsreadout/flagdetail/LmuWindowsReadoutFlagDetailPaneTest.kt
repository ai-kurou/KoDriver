package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
    fun `イエローフラッグのカスタム文言を入力するとonFlagTextChangedが呼ばれる`() {
        var changedText: String? = null
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.SectorYellowFlag to ""),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { _, text -> changedText = text },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("イエローフラッグ")[1].performTextInput("イエロー、注意")
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
    fun `全フラッグはチップを表示せず既定文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        // カードのタイトルと入力欄の既定文言のみ（チップがあれば3つになる）。
        rule.onAllNodesWithText("ブルーフラッグ").assertCountEquals(2)
        rule.onAllNodesWithText("イエローフラッグ").assertCountEquals(2)
        rule.onAllNodesWithText("フルコースイエロー").assertCountEquals(2)
        rule.onAllNodesWithText("レッドフラッグ").assertCountEquals(2)
    }

    @Test
    fun `TTSを利用できない場合はカスタム文言を入力できない`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = false),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
    }

    @Test
    fun `カスタム文言を1文字入力しただけで確定操作なしに入力欄が選択状態になる`() {
        var uiState by mutableStateOf(
            LmuWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.FullCourseYellow to ""),
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = uiState,
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { item, text ->
                        uiState = uiState.copy(flagTexts = mapOf(item to text))
                    },
                    onFlagTextPreviewClicked = { _, _ -> },
                )
            }
        }

        rule.onAllNodesWithText("フルコースイエロー")[1].performTextInput("イ")

        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithText("フルコースイエロー").assertCountEquals(1)
    }

    @Test
    fun `レッドフラッグのカスタム文言を入力するとレッドフラッグでonFlagTextChangedとonFlagTextPreviewClickedが呼ばれる`() {
        val changes = mutableListOf<Pair<FlagReadoutItem, String>>()
        val previews = mutableListOf<Pair<FlagReadoutItem, String>>()
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.RedFlag to ""),
                            isTextToSpeechAvailable = true,
                        ),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { item, text -> changes += item to text },
                    onFlagTextPreviewClicked = { item, text -> previews += item to text },
                )
            }
        }

        rule.onAllNodesWithText("レッドフラッグ")[1].performScrollTo().performTextInput("赤旗、停止")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].performScrollTo().performClick()

        assertEquals(listOf(FlagReadoutItem.RedFlag to "赤旗、停止"), changes)
        assertEquals(listOf(FlagReadoutItem.RedFlag to "赤旗、停止"), previews)
    }
}
