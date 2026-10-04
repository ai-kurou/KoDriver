package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class LmuWindowsReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `リセットボタンをクリックするとonVehicleClassHighThresholdResetが選択中クラスで呼ばれる`() {
        var resetVehicleClass: LmuWindowsVehicleClassData? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Hypercar to 95),
                        ),
                    onVehicleClassHighThresholdReset = { resetVehicleClass = it },
                )
            }
        }
        rule.onNodeWithContentDescription("デフォルトに戻す").performClick()
        assertEquals(LmuWindowsVehicleClassData.Hypercar, resetVehicleClass)
    }

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                )
            }
        }

        rule.onNodeWithContentDescription("高温閾値の説明を表示").performClick()

        rule.onNodeWithText("設定した温度以上になると過熱警告を読み上げます", substring = true).assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定するとonVehicleClassHighThresholdChangedが選択中クラスで呼ばれる`() {
        var changedVehicleClass: LmuWindowsVehicleClassData? = null
        var changedValue: Int? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Hypercar to 90),
                        ),
                    onVehicleClassHighThresholdChanged = { vehicleClass, celsius ->
                        changedVehicleClass = vehicleClass
                        changedValue = celsius
                    },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..110f, steps = 19)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(95f) }

        assertEquals(LmuWindowsVehicleClassData.Hypercar, changedVehicleClass)
        assertEquals(95, changedValue)
    }

    @Test
    fun `スライダーは選択中クラスの閾値を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 97,
                                ),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
                        ),
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 97f, range = 90f..110f, steps = 19)),
            ).assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのヘッダーをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `低温文言入力欄が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(lowWarningEnabled = true),
                )
            }
        }

        rule.onAllNodesWithText("タイヤが冷えているときの文言", substring = true)[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `対象クラスのサブタイトルとクラス別閾値のチップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Gt3 to 90,
                                    LmuWindowsVehicleClassData.Unknown("") to 95,
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithText("対象クラス").assertIsDisplayed()
        rule.onNodeWithText("GT3（90°C）").assertIsDisplayed()
    }

    @Test
    fun `対象クラスのチップはHyperがデフォルトで選択されている`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 95,
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithText("Hyper（90°C）").assertIsSelected()
        rule.onNodeWithText("GT3（95°C）").assertIsNotSelected()
    }

    @Test
    fun `対象クラスのチップは選択済みのselectedVehicleClassが選択状態になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 95,
                                ),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
                        ),
                )
            }
        }

        rule.onNodeWithText("GT3（95°C）").assertIsSelected()
        rule.onNodeWithText("Hyper（90°C）").assertIsNotSelected()
    }

    @Test
    fun `対象クラスのチップをクリックするとonVehicleClassSelectedにそのクラスが渡される`() {
        var selectedVehicleClass: LmuWindowsVehicleClassData? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 95,
                                ),
                        ),
                    onVehicleClassSelected = { selectedVehicleClass = it },
                )
            }
        }

        rule.onNodeWithText("GT3（95°C）").performClick()

        assertEquals(LmuWindowsVehicleClassData.Gt3, selectedVehicleClass)
    }

    @Test
    fun `過熱文言の試聴は選択中クラスの現在の閾値を渡す`() {
        var selectedClass by mutableStateOf<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
        val thresholds = mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gt3 to 107)
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            isTextToSpeechAvailable = true,
                            vehicleClassHighThresholdCelsius = thresholds,
                            selectedVehicleClass = selectedClass,
                        ),
                    onOverheatReadoutTextPreviewClicked = { text, celsius -> previews += text to celsius },
                )
            }
        }
        val expected = mutableListOf<Pair<String, Int>>()
        tyreTemperatureVehicleClasses.forEach { vehicleClass ->
            selectedClass = vehicleClass
            rule.waitForIdle()
            rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
            val celsius =
                thresholds[vehicleClass]
                    ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass).value
            expected += LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to celsius
        }
        assertEquals(expected, previews)
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
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
    fun `保存済みの古い文言が流れてきても入力中の文言を巻き戻さず一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("あい")
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
    fun `前後に空白がある入力も保存済みの正規化後の文言と一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement(" あい ")
        savedText = "あい"
        rule.waitForIdle()
        savedText = "う"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
    }

    @Test
    fun `空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(overheatReadoutText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
        rule.onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[0].assertExists()
    }

    @Test
    fun `低温 スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            lowWarningEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = { changed += it },
                    onLowWarningPreviewClicked = { previews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[1]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performScrollTo().performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `低温 保存済みの古い文言が流れてきても入力中の文言を巻き戻さず一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            coldReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("あい")
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
    fun `低温 前後に空白がある入力も保存済みの正規化後の文言と一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            coldReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement(" あい ")
        savedText = "あい"
        rule.waitForIdle()
        savedText = "う"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
    }

    @Test
    fun `低温 空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            coldReadoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsEnabled()
    }

    @Test
    fun `低温 TTS不可では入力と試聴を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(coldReadoutText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsNotEnabled()
        rule.onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[1].assertExists()
    }

    @Test
    fun `低温文言の編集とリセットは過熱文言を変更しない`() {
        val coldChanges = mutableListOf<String>()
        val overheatChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "冷やして",
                            coldReadoutText = "温めて",
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = { coldChanges += it },
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("低温注意")
        rule.onNodeWithContentDescription("低温警告の文言をデフォルトに戻す").performScrollTo().performClick()
        assertEquals(listOf("低温注意", LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT), coldChanges)
        assertEquals(emptyList<String>(), overheatChanges)
        rule.onNode(hasSetTextAction() and hasText("冷やして")).assertExists()
        rule
            .onNode(hasSetTextAction() and hasText(LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT))
            .assertExists()
    }

    @Test
    fun `過熱文言が既定値と異なるとリセットボタンを表示しクリックで既定文言を保存する`() {
        val overheatChanges = mutableListOf<String>()
        val coldChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "冷やして",
                            coldReadoutText = "温めて",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                    onColdReadoutTextChanged = { coldChanges += it },
                )
            }
        }
        rule
            .onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT), overheatChanges)
        assertEquals(emptyList<String>(), coldChanges)
        rule
            .onNode(hasSetTextAction() and hasText(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT))
            .assertExists()
        rule.onNode(hasSetTextAction() and hasText("温めて")).assertExists()
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `過熱文言が既定値と一致するとリセットボタンは無効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithContentDescription("過熱警告の文言をデフォルトに戻す").assertIsNotEnabled()
    }

    @Test
    fun `温度チップは入力末尾に追加して過熱と低温の保存コールバックへ渡す`() {
        val overheatChanges = mutableListOf<String>()
        val coldChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "過熱",
                            coldReadoutText = "低温",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                    onColdReadoutTextChanged = { coldChanges += it },
                )
            }
        }
        rule.onAllNodesWithText("{celsius}を挿入")[0].performScrollTo().performClick()
        rule.onAllNodesWithText("{celsius}を挿入")[1].performScrollTo().performClick()
        assertEquals(listOf("過熱{celsius}"), overheatChanges)
        assertEquals(listOf("低温{celsius}"), coldChanges)
        rule.onNode(hasSetTextAction() and hasText("過熱{celsius}")).assertExists()
        rule.onNode(hasSetTextAction() and hasText("低温{celsius}")).assertExists()
    }

    @Test
    fun `温度チップは上限ちょうどまで追加でき上限を超えると無効になる`() {
        val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length)
        val overheatChanges = mutableListOf<String>()
        val coldChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = text,
                            coldReadoutText = text,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                    onColdReadoutTextChanged = { coldChanges += it },
                )
            }
        }
        listOf(0, 1).forEach { index ->
            rule
                .onAllNodesWithText("{celsius}を挿入")[index]
                .performScrollTo()
                .assertIsEnabled()
                .performClick()
            rule.onAllNodesWithText("{celsius}を挿入")[index].assertIsNotEnabled()
        }
        assertEquals(listOf(text + LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER), overheatChanges)
        assertEquals(overheatChanges, coldChanges)
    }

    @Test
    fun `未知のプレースホルダーは出現順で重複なく警告し既知の温度は警告しない`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "{celsius}{wheel}{x}{wheel}",
                            coldReadoutText = "{celsius}{wheel}{x}{wheel}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        listOf(0, 1).forEach { index ->
            rule
                .onAllNodesWithText("未対応のプレースホルダー {wheel}、{x} はそのまま読み上げられます")[index]
                .performScrollTo()
                .assertIsDisplayed()
        }
        rule.onAllNodesWithText("未対応のプレースホルダー {celsius}", substring = true).assertCountEquals(0)
    }

    @Test
    fun `TTS利用不可では温度チップを無効にして未知トークンより利用不可案内を優先する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "{wheel}",
                            coldReadoutText = "{wheel}",
                            isTextToSpeechAvailable = false,
                        ),
                )
            }
        }
        listOf(0, 1).forEach { index ->
            rule.onAllNodesWithText("{celsius}を挿入")[index].performScrollTo().assertIsNotEnabled()
            rule.onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[index].assertExists()
        }
        rule.onAllNodesWithText("未対応のプレースホルダー", substring = true).assertCountEquals(0)
    }
}
