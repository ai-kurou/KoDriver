package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val DEFAULT_TEXT = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent()
            }
        }

        rule.onNodeWithText("読み上げる文言は下の欄で設定できます。", substring = true).assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("燃料残り周回数").assertIsDisplayed()
        rule.onNodeWithText("1周以上のときの文言").assertIsDisplayed()
        rule.onNodeWithText("燃料は残り約{laps}周").assertIsDisplayed()
        rule.onNodeWithText("{laps} は残り周回数に置き換わります").assertIsDisplayed()
        rule.onNodeWithText("{laps}を挿入").assertIsEnabled()
        rule.onNodeWithText("燃料がありません(1.0周未満)のときの文言").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("燃料残り1周未満").assertIsDisplayed()
        rule.onNodeWithText("残り約: 3 周").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            enabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changed += it },
                    onReadoutTextPreviewClicked = { text, laps ->
                        previews += text
                        assertEquals(3, laps)
                    },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `保存済みの古い文言が流れてきても入力中の文言を巻き戻さず一致したら同期する`() {
        var savedText by mutableStateOf("")
        var savedEmptyText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = savedText,
                            emptyReadoutText = savedEmptyText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("あい")
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("燃料なし")
        rule.runOnIdle {
            savedText = "あ"
            savedEmptyText = "燃料"
        }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("あい")).assertExists()
        rule.onNode(hasSetTextAction() and hasText("燃料なし")).assertExists()
        rule.runOnIdle {
            savedText = "あい"
            savedEmptyText = "燃料なし"
        }
        rule.waitForIdle()
        rule.runOnIdle {
            savedText = "う"
            savedEmptyText = "保存後"
        }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
        rule.onNode(hasSetTextAction() and hasText("保存後")).assertExists()
    }

    @Test
    fun `前後に空白を含む入力はtrim後の保存値で待機を解除し以降の更新を反映する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement(" あい ")
        rule.runOnIdle { savedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `入力直後の文言にlapsを挿入し上限ちょうどで無効になる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                    onReadoutTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(24)
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement(text)
        rule.onNodeWithText("{laps}を挿入").assertIsEnabled().performClick()
        assertEquals(listOf(text, text + "{laps}"), changed)
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `挿入で文字数上限を超える場合は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = "あ".repeat(25),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `未知プレースホルダーの警告が表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = "{lap}{x}{lap}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{laps} を使用してください").assertExists()
    }

    @Test
    fun `空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = "",
                            emptyReadoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodesWithText("空欄のままなら読み上げません").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsEnabled()
    }

    @Test
    fun `TTS不可では利用不可案内を優先し入力と試聴と挿入を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = "{lap}",
                            emptyReadoutText = "編集済み燃料なし文言",
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onNodeWithText("編集済み燃料なし文言").assertIsNotEnabled()
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
        rule.onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[0].assertExists()
    }

    @Test
    fun `スライダーは1から5周の範囲で変更とリセットを通知する`() {
        var changed: Int? = null
        var reset = false
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 4),
                    onRemainingFuelLapsChanged = { changed = it },
                    onResetRemainingFuelLaps = { reset = true },
                )
            }
        }
        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(4f, 1f..5f, 3)))
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        assertEquals(5, changed)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[2].performScrollTo().performClick()
        assertEquals(true, reset)
    }

    @Test
    fun `スイッチをタップするとonEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(enabled = true),
                    onEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("燃料残り周回数").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `編集後のリセットは既定文言を表示して保存を通知する`() {
        val changes = mutableListOf<String>()
        setResetTestContent(changes = changes)

        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("編集済み文言")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsEnabled().performClick()
        rule.onAllNodes(hasSetTextAction())[0].assertEditableTextEquals(DEFAULT_TEXT)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
        assertEquals(listOf("編集済み文言", DEFAULT_TEXT), changes)
    }

    @Test
    fun `既定文言と同じならリセットできず保存も通知しない`() {
        val changes = mutableListOf<String>()
        setResetTestContent(changes = changes)

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled().performClick()
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `TTS利用不可では編集済み文言をリセットできず保存も通知しない`() {
        val changes = mutableListOf<String>()
        setResetTestContent(
            changes = changes,
            initialState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(readoutText = "編集済み文言"),
        )

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled().performClick()
        rule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))[0]
            .assertEditableTextEquals("編集済み文言")
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `保存待ちのリセットは古い文言で巻き戻らず保存完了後は同期する`() {
        val changes = mutableListOf<String>()
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changes += it },
                )
            }
        }

        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("編集済み文言")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].performClick()
        rule.onAllNodes(hasSetTextAction())[0].assertEditableTextEquals(DEFAULT_TEXT)
        rule.runOnIdle { savedText = "編集済み文言" }
        rule.waitForIdle()
        rule.onAllNodes(hasSetTextAction())[0].assertEditableTextEquals(DEFAULT_TEXT)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
        rule.runOnIdle { savedText = DEFAULT_TEXT }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "保存後の変更" }
        rule.waitForIdle()
        rule.onAllNodes(hasSetTextAction())[0].assertEditableTextEquals("保存後の変更")
        assertEquals(listOf("編集済み文言", DEFAULT_TEXT), changes)
    }

    @Test
    fun `燃料なし用の欄は編集と試聴とリセットを通知しプレースホルダー警告を表示しない`() {
        val changes = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                    onEmptyReadoutTextChanged = { changes += it },
                    onEmptyReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule.onNodeWithText("燃料がありません(1.0周未満)のときの文言").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled().performClick()
        rule.onAllNodes(hasSetTextAction())[1].performTextReplacement("あ".repeat(31))
        rule.onAllNodes(hasSetTextAction())[1].assertEditableTextEquals("あ".repeat(30))
        rule.onAllNodes(hasSetTextAction())[1].performTextReplacement("燃料なし{x}")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performClick()
        rule.onNodeWithText("{x} は置き換えられません。{laps} を使用してください").assertDoesNotExist()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performClick()
        rule.onAllNodes(hasSetTextAction())[1].assertEditableTextEquals("燃料残り1周未満")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
        assertEquals(listOf("あ".repeat(30), "燃料なし{x}", "燃料残り1周未満"), changes)
        assertEquals(listOf("燃料なし{x}"), previews)
    }

    private fun setResetTestContent(
        changes: MutableList<String>,
        initialState: Gt7Ps5ReadoutRemainingFuelLapsDetailUiState =
            Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = uiState,
                    onReadoutTextChanged = {
                        changes += it
                        uiState = uiState.copy(readoutText = it)
                    },
                )
            }
        }
    }

    private fun SemanticsNodeInteraction.assertEditableTextEquals(expected: String) =
        assert(
            SemanticsMatcher("EditableText == $expected") {
                it.config.getOrNull(SemanticsProperties.EditableText)?.text == expected
            },
        )
}
