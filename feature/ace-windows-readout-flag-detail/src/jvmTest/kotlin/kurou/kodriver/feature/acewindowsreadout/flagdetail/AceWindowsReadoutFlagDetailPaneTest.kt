@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutFlagDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とブルーフラッグカードが表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        rule
            .onNodeWithText(
                "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
            ).assertIsDisplayed()
        rule.onAllNodesWithText("ブルーフラッグ")[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `フラッグカードをタップするとonFlagEnabledChangedが呼ばれる`() {
        var changedItem: FlagReadoutItem? = null
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = AceWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { item, enabled ->
                        changedItem = item
                        changedEnabled = enabled
                    },
                )
            }
        }

        rule
            .onAllNodesWithText("ブルーフラッグ")[0]
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals(FlagReadoutItem.BlueFlag, changedItem)
        assertEquals(false, changedEnabled)
    }

    @Test
    fun `自由文言10種の入力欄とTTS試聴とリセットを表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = AceWindowsReadoutFlagDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onAllNodesWithContentDescription("入力した文言を再生").assertCountEquals(10)
        rule.onAllNodesWithContentDescription("デフォルトに戻す").assertCountEquals(10)
        rule.onAllNodesWithText("チェッカーフラッグ").assertCountEquals(2)
        listOf(
            "ホワイトフラッグ",
            "グリーンフラッグ",
            "レッドフラッグ",
            "ブルーフラッグ",
            "イエローフラッグ",
            "ブラックフラッグ",
            "ブラック・ホワイトフラッグ",
        ).forEach { label -> rule.onAllNodesWithText(label).assertCountEquals(2) }
        rule.onAllNodesWithText("オレンジボールフラッグ").assertCountEquals(1)
        rule.onAllNodesWithText("レッド・イエローストライプフラッグ").assertCountEquals(1)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[7].assertIsNotEnabled()
    }

    @Test
    fun `Checkeredの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to ""),
                isTextToSpeechAvailable = true,
            ),
        )
        val changes = mutableListOf<Pair<FlagReadoutItem, String>>()
        val previews = mutableListOf<String>()
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = uiState,
                    onFlagTextChanged = { item, text ->
                        changes += item to text
                        uiState = uiState.copy(flagTexts = mapOf(item to text))
                    },
                    onFlagTextPreviewClicked = { previews += it },
                )
            }
        }
        rule.onAllNodesWithText("チェッカーフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.CheckeredFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[7].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[7].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みCheckeredのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to "編集済み"),
                isTextToSpeechAvailable = true,
            ),
        )
        val resets = mutableListOf<FlagReadoutItem>()
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = uiState,
                    onFlagTextReset = {
                        resets += it
                        uiState = uiState.copy(flagTexts = mapOf(it to it.defaultText))
                    },
                )
            }
        }
        rule
            .onAllNodesWithContentDescription("デフォルトに戻す")[7]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.CheckeredFlag), resets)
        rule.onAllNodesWithText("チェッカーフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[7].assertIsNotEnabled()
    }

    @Test
    fun `TTS利用不可では全10種の入力と試聴とリセットを無効にする`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        AceWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to "編集済み"),
                            isTextToSpeechAvailable = false,
                        ),
                )
            }
        }
        FlagReadoutItem.entries.forEachIndexed { index, item ->
            val text = if (item == FlagReadoutItem.CheckeredFlag) "編集済み" else item.defaultText
            rule.onAllNodesWithText(text)[if (index < 7) 1 else 0].performScrollTo().assertIsNotEnabled()
            rule.onAllNodesWithContentDescription("入力した文言を再生")[index].assertIsNotEnabled()
            rule.onAllNodesWithContentDescription("デフォルトに戻す")[index].assertIsNotEnabled()
            rule
                .onAllNodesWithText("この端末では音声合成を利用できないため、自由文言のフラッグは読み上げません")[index]
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    @Test
    fun `空白文言は読み上げない補助文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState =
                        AceWindowsReadoutFlagDetailUiState(
                            flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to ""),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("空欄のままなら読み上げません").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithContentDescription("この文言を読み上げます").assertCountEquals(9)
    }

    @Test
    fun `保存時にtrimされた文言を入力欄に反映し次の変更も保存する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to "完走"),
                isTextToSpeechAvailable = true,
            ),
        )
        val saved = mutableListOf<String>()
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent(
                    uiState = uiState,
                    onFlagTextChanged = { item, text ->
                        val normalized = text.trim()
                        saved += normalized
                        uiState = uiState.copy(flagTexts = mapOf(item to normalized))
                    },
                )
            }
        }
        rule.onNodeWithText("完走").performScrollTo().performTextReplacement("  注意  ")
        rule.onNodeWithText("注意").assertIsDisplayed()
        rule.onNodeWithText("2/30").assertIsDisplayed()
        rule.onNodeWithText("注意").performTextReplacement("停止")
        assertEquals(listOf("注意", "停止"), saved)
    }
}
