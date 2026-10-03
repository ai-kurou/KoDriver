package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutRemainingVirtualEnergyDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
            }
        }

        rule.onNodeWithText("バーチャルエナジー残量が設定した閾値以下になった場合に音声でお知らせします。\n読み上げる文言は下の欄で設定できます。").assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("残量警告").assertIsDisplayed()
        rule.onNodeWithText("残量が閾値以下になったときの文言").assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー残量{percent}%以下").assertIsDisplayed()
        rule.onNodeWithText("{percent} は残量閾値(%)に置き換わります").assertIsDisplayed()
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                            enabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changed += it },
                    onReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `保存済みの古い文言が流れてきても入力中の文言を巻き戻さず一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("あい")
        savedText = "あ"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("あい")).assertExists()
        savedText = "あい"
        rule.waitForIdle()
        savedText = "う"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
    }

    @Test
    fun `入力直後の文言にpercentを挿入し上限ちょうどで無効になる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(isTextToSpeechAvailable = true),
                    onReadoutTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(21)
        rule.onNode(hasSetTextAction()).performTextReplacement(text)
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled().performClick()
        assertEquals(listOf(text, text + "{percent}"), changed)
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `挿入で文字数上限を超える場合は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                            readoutText = "あ".repeat(22),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `未知プレースホルダーの警告が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                            readoutText = "{lap}{x}{lap}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{percent} を使用してください").assertExists()
    }

    @Test
    fun `空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                            readoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsEnabled()
    }

    @Test
    fun `TTS不可では利用不可案内を優先し入力と試聴と挿入を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(readoutText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、読み上げません").assertExists()
    }

    @Test
    fun `閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
            }
        }

        rule.onNodeWithText("残量閾値").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("残量が30%以下になると警告を読み上げます。").assertIsDisplayed()
        rule.onNodeWithText("30%").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(thresholdPercentage = 50),
                    onThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNodeWithContentDescription("デフォルトに戻す").performScrollTo().performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `スイッチをタップするとonEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
                    uiState = LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(enabled = true),
                    onEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("残量警告").performClick()

        assertEquals(false, changedEnabled)
    }
}
