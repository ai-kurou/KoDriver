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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutFlagDetailCommonFlagPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Whiteの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.WhiteFlag to ""),
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
        rule.onAllNodesWithText("ホワイトフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.WhiteFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[0].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みWhiteのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.WhiteFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[0]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.WhiteFlag), resets)
        rule.onAllNodesWithText("ホワイトフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
    }

    @Test
    fun `Greenの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.GreenFlag to ""),
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
        rule.onAllNodesWithText("グリーンフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.GreenFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[1].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みGreenのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.GreenFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[1]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.GreenFlag), resets)
        rule.onAllNodesWithText("グリーンフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
    }

    @Test
    fun `Redの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.RedFlag to ""),
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
        rule.onAllNodesWithText("レッドフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.RedFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[2].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みRedのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.RedFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[2]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.RedFlag), resets)
        rule.onAllNodesWithText("レッドフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[2].assertIsNotEnabled()
    }

    @Test
    fun `Blueの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlueFlag to ""),
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
        rule.onAllNodesWithText("ブルーフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.BlueFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[3].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みBlueのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlueFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[3]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.BlueFlag), resets)
        rule.onAllNodesWithText("ブルーフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[3].assertIsNotEnabled()
    }

    @Test
    fun `Yellowの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.YellowFlag to ""),
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
        rule.onAllNodesWithText("イエローフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.YellowFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[4].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[4].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みYellowのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.YellowFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[4]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.YellowFlag), resets)
        rule.onAllNodesWithText("イエローフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[4].assertIsNotEnabled()
    }

    @Test
    fun `Blackの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlackFlag to ""),
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
        rule.onAllNodesWithText("ブラックフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.BlackFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[5].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[5].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みBlackのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlackFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[5]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.BlackFlag), resets)
        rule.onAllNodesWithText("ブラックフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[5].assertIsNotEnabled()
    }

    @Test
    fun `BlackWhiteの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlackWhiteFlag to ""),
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
        rule.onAllNodesWithText("ブラック・ホワイトフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.BlackWhiteFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[6].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[6].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みBlackWhiteのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.BlackWhiteFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[6]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.BlackWhiteFlag), resets)
        rule.onAllNodesWithText("ブラック・ホワイトフラッグ").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[6].assertIsNotEnabled()
    }

    @Test
    fun `OrangeCircleの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.OrangeCircleFlag to ""),
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
        rule.onAllNodesWithText("オレンジボールフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.OrangeCircleFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[8].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[8].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みOrangeCircleのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.OrangeCircleFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[8]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.OrangeCircleFlag), resets)
        rule.onAllNodesWithText("オレンジボールフラッグ").assertCountEquals(1)
        rule.onAllNodesWithText("オレンジボールフラッグ、車両に不具合があります").assertCountEquals(1)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[8].assertIsNotEnabled()
    }

    @Test
    fun `RedYellowStripesの入力は即時保存通知して入力中の文言を試聴する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.RedYellowStripesFlag to ""),
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
        rule.onAllNodesWithText("レッド・イエローストライプフラッグ")[1].performScrollTo().performTextInput("完走")
        assertEquals(listOf(FlagReadoutItem.RedYellowStripesFlag to "完走"), changes)
        rule.onAllNodesWithContentDescription("この文言を読み上げます")[9].assertIsDisplayed()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[9].performScrollTo().performClick()
        assertEquals(listOf("完走"), previews)
    }

    @Test
    fun `編集済みRedYellowStripesのリセットは対応項目を通知して既定値を表示する`() {
        var uiState by mutableStateOf(
            AceWindowsReadoutFlagDetailUiState(
                flagTexts = mapOf(FlagReadoutItem.RedYellowStripesFlag to "編集済み"),
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
            .onAllNodesWithContentDescription("デフォルトに戻す")[9]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(FlagReadoutItem.RedYellowStripesFlag), resets)
        rule.onAllNodesWithText("レッド・イエローストライプフラッグ").assertCountEquals(1)
        rule.onAllNodesWithText("レッド・イエローストライプフラッグ、路面が滑りやすいです").assertCountEquals(1)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[9].assertIsNotEnabled()
    }
}
