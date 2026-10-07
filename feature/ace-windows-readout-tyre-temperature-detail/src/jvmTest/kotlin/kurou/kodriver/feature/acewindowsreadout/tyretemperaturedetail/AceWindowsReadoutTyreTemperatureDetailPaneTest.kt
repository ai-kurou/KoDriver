package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class AceWindowsReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤ温度").assertIsDisplayed()
        rule
            .onNodeWithText(
                "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、" +
                    "ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
            ).assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのタイトルと文言と高温閾値設定が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("過熱警告").assertIsDisplayed()
        rule.onNodeWithText("タイヤが過熱したときの文言").assertIsDisplayed()
        rule.onNodeWithText("高温閾値設定").assertIsDisplayed()
        rule.onNodeWithText("高温閾値: 90°C").assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのスイッチをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `高温閾値スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() {
        var changedCelsius: Int? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    onHighThresholdChanged = { changedCelsius = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..110f, steps = 19)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(105f)
            }

        assertEquals(105, changedCelsius)
    }

    @Test
    fun `デフォルト値から変更している場合に高温閾値のリセットボタンをタップするとonHighThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = 105),
                    onHighThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("デフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatWarningEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                    onOverheatReadoutTextPreviewClicked = { text, _ -> previews += text },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsEnabled()
    }

    @Test
    fun `TTS不可では入力と試聴を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatReadoutText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
        rule.onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[0].assertExists()
    }

    @Test
    fun `過熱文言が既定値と一致するとリセットボタンは無効になる`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `文言の試聴は画面表示中の閾値を渡す`() {
        var threshold by mutableIntStateOf(105)
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            highThresholdCelsius = threshold,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextPreviewClicked = { text, celsius -> previews += text to celsius },
                )
            }
        }
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        rule.runOnIdle { threshold = 107 }
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        assertEquals(
            listOf(
                ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to 105,
                ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to 107,
            ),
            previews,
        )
    }

    @Test
    fun `文言をリセットすると既定文言を保存し表示する`() {
        val changes = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "冷やして",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changes += it },
                )
            }
        }
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").performClick()
        assertEquals(listOf(ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT), changes)
        rule
            .onNode(hasSetTextAction() and hasText(ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT))
            .assertExists()
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `温度チップは入力末尾へ上限ちょうどまで追加でき上限を超えると無効になる`() {
        val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length)
        val changes = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = text,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changes += it },
                )
            }
        }
        rule.onNodeWithText("{celsius}を挿入").assertIsEnabled().performClick()
        rule.onNodeWithText("{celsius}を挿入").assertIsNotEnabled()
        assertEquals(listOf(text + ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER), changes)
        rule.onNode(hasSetTextAction() and hasText(changes.single())).assertExists()
    }

    @Test
    fun `未知のプレースホルダーは重複なく警告し既知の温度は警告しない`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "{celsius}{wheel}{x}{wheel}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("未対応のプレースホルダー {wheel}、{x} はそのまま読み上げられます").assertIsDisplayed()
        rule.onAllNodesWithText("未対応のプレースホルダー {celsius}", substring = true).assertCountEquals(0)
        rule
            .onNodeWithText("{celsius} は警告時点で最も高温のタイヤのカーカス温度(℃)を整数に丸めた値に置き換わります")
            .assertIsDisplayed()
    }

    @Test
    fun `TTS利用不可では温度チップとリセットを無効にし利用不可案内を優先する`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = AceWindowsReadoutTyreTemperatureDetailUiState(overheatReadoutText = "{wheel}"),
                )
            }
        }
        rule.onNodeWithText("{celsius}を挿入").assertIsNotEnabled()
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、読み上げません").assertExists()
        rule.onAllNodesWithText("未対応のプレースホルダー", substring = true).assertCountEquals(0)
    }
}
