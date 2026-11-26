package kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail

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
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class AceWindowsAutoReadoutBrakeTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `過熱文言の中間のカーソル位置にcelsiusを挿入して保存通知する`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatReadoutText = "前半後半",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                )
            }
        }
        rule.onNode(hasSetTextAction() and hasText("前半後半")).performTextInputSelection(TextRange(2))
        rule.onNodeWithText("{celsius}を挿入").performClick()
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("前半{celsius}後半")).assertExists()
        assertEquals(listOf("前半{celsius}後半"), changed)
    }

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent()
            }
        }

        rule
            .onNodeWithText("いずれかのブレーキが過熱閾値以上になると読み上げます。一度過熱を判定すると、すべてのブレーキが閾値より100℃以上下がるまで再び読み上げません。")
            .assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのタイトルと文言と過熱閾値設定が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent()
            }
        }

        rule.onNodeWithText("過熱警告").assertIsDisplayed()
        rule.onNodeWithText("ブレーキ温度が閾値以上になったときの文言").assertIsDisplayed()
        rule.onNodeWithText("過熱閾値").assertIsDisplayed()
        rule.onNodeWithText("800℃", substring = false).assertExists()
        rule.onNodeWithText("過熱とみなすブレーキ温度は800℃以上です。").assertExists()
    }

    @Test
    fun `過熱警告カードのスイッチをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState = AceWindowsAutoReadoutBrakeTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `過熱閾値スライダーの値を確定するとonHighThresholdChangedが呼ばれる`() {
        var changedCelsius: Int? = null
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    onHighThresholdChanged = { changedCelsius = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 800f, range = 600f..1000f, steps = 399)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(905f)
            }

        assertEquals(905, changedCelsius)
    }

    @Test
    fun `デフォルト値から変更している場合に過熱閾値のリセットボタンをタップするとonHighThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState = AceWindowsAutoReadoutBrakeTemperatureDetailUiState(highThresholdCelsius = 905),
                    onHighThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onNode(hasContentDescription("過熱閾値をデフォルトに戻す")).performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatWarningEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                    onOverheatReadoutTextPreviewClicked = { text, _ -> previews += text },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].assertIsEnabled().performTextReplacement(
            "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1),
        )
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `TTS不可では入力と試聴を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState = AceWindowsAutoReadoutBrakeTemperatureDetailUiState(overheatReadoutText = "{lap}"),
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
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatReadoutText = ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `文言の試聴は画面表示中の閾値を渡す`() {
        var threshold by mutableIntStateOf(905)
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            highThresholdCelsius = threshold,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextPreviewClicked = { text, celsius -> previews += text to celsius },
                )
            }
        }
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        rule.runOnIdle { threshold = 907 }
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        assertEquals(
            listOf(
                ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to 905,
                ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to 907,
            ),
            previews,
        )
    }

    @Test
    fun `文言をリセットすると既定文言を保存し表示する`() {
        val changes = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatReadoutText = "冷やして",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changes += it },
                )
            }
        }
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").performClick()
        assertEquals(listOf(ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT), changes)
        rule
            .onNode(hasSetTextAction() and hasText(ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT))
            .assertExists()
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `温度チップは入力末尾へ上限ちょうどまで追加でき上限を超えると無効になる`() {
        val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER.length)
        val changes = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatReadoutText = text,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changes += it },
                )
            }
        }
        rule.onNodeWithText("{celsius}を挿入").assertIsEnabled().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("{celsius}を挿入").assertIsNotEnabled()
        assertEquals(listOf(text + ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER), changes)
        rule.onNode(hasSetTextAction() and hasText(changes.single())).assertExists()
    }

    @Test
    fun `未知のプレースホルダーは重複なく警告し既知の温度は警告しない`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState =
                        AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
                            overheatReadoutText = "{celsius}{wheel}{x}{wheel}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("未対応のプレースホルダー {wheel}、{x} はそのまま読み上げられます").assertIsDisplayed()
        rule.onAllNodesWithText("未対応のプレースホルダー {celsius}", substring = true).assertCountEquals(0)
        rule
            .onNodeWithText("{celsius} は過熱閾値（℃）に置き換わります")
            .assertIsDisplayed()
    }

    @Test
    fun `TTS利用不可では温度チップとリセットを無効にし利用不可案内を優先する`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState = AceWindowsAutoReadoutBrakeTemperatureDetailUiState(overheatReadoutText = "{wheel}"),
                )
            }
        }
        rule.onNodeWithText("{celsius}を挿入").assertIsNotEnabled()
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、読み上げません").assertExists()
        rule.onAllNodesWithText("未対応のプレースホルダー", substring = true).assertCountEquals(0)
    }
}
