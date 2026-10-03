package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

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
    fun `タイヤ低温警告チップをタップするとonLowWarningPreviewClickedが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                    onLowWarningPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].performScrollTo().performClick()

        assertEquals(true, previewClicked)
    }

    @Test
    fun `タイヤ低温警告チップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(lowWarningEnabled = true),
                )
            }
        }

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].performScrollTo().assertIsDisplayed()
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
                    onOverheatReadoutTextPreviewClicked = { previews += it },
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
        rule.onNode(hasSetTextAction()).performTextReplacement(" あい ")
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
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsEnabled()
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
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、読み上げません").assertExists()
    }
}
