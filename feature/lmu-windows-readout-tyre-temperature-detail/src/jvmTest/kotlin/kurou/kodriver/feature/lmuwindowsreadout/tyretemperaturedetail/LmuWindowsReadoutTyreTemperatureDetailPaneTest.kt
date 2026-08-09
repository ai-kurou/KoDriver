package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
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
    fun `スライダーは選択中クラスのしきい値を表示する`() {
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
    fun `タイヤ過熱警告チップをタップするとonPreviewClickedが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
                    onPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onAllNodesWithText("タイヤ過熱警告", substring = true)[0].performClick()

        assertEquals(true, previewClicked)
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

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].performClick()

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

        rule.onAllNodesWithText("タイヤ低温警告", substring = true)[0].assertIsDisplayed()
    }

    @Test
    fun `対象クラスのサブタイトルとクラス別しきい値のチップが表示される`() {
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
}
