package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutTyreTemperatureDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutTyreTemperatureDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映して設定と文言とTTS可否を保持する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutTyreTemperatureDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutTyreTemperatureDetailUiState()
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false,
                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(overheatWarningEnabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to false,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(overheatWarningEnabled = true, lowWarningEnabled = false)
            assertEquals(expected, states.last())

            inputs.lowWarningPhases.update { emptySet() }
            testScheduler.runCurrent()
            expected = expected.copy(lowWarningPhases = emptySet())
            assertEquals(expected, states.last())

            inputs.vehicleClassHighThreshold.update {
                mapOf(
                    LmuWindowsVehicleClassData.Hypercar to Celsius(110),
                    LmuWindowsVehicleClassData.P2 to Celsius(105),
                )
            }
            testScheduler.runCurrent()
            expected =
                expected.copy(
                    vehicleClassHighThresholdCelsius =
                        mapOf(LmuWindowsVehicleClassData.Hypercar to 110, LmuWindowsVehicleClassData.P2 to 105),
                )
            assertEquals(expected, states.last())

            inputs.vehicleClassSelection.update { LmuWindowsVehicleClassData.P2 }
            testScheduler.runCurrent()
            expected = expected.copy(selectedVehicleClass = LmuWindowsVehicleClassData.P2)
            assertEquals(expected, states.last())

            inputs.overheatReadoutText.update { "過熱 {celsius}度" }
            testScheduler.runCurrent()
            expected = expected.copy(overheatReadoutText = "過熱 {celsius}度")
            assertEquals(expected, states.last())

            inputs.coldReadoutText.update { "低温 {celsius}度" }
            testScheduler.runCurrent()
            expected = expected.copy(coldReadoutText = "低温 {celsius}度")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.vehicleClassHighThreshold.update { emptyMap() }
            testScheduler.runCurrent()
            expected = expected.copy(vehicleClassHighThresholdCelsius = emptyMap())
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { false }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = false)
            assertEquals(expected, states.last())
        }

    @Test
    fun `購読時だけ上流を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)

            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(7, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(14, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.coldReadoutText.update { "再購読" }
            assertEquals(LmuWindowsAutoReadoutTyreTemperatureDetailUiState(coldReadoutText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val enabledStates =
            MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                mapOf(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to true,
                ),
            )
        val lowWarningPhases = MutableStateFlow<Set<SessionPhase>>(lmuWindowsTyreTemperatureLowWarningDefaultPhases)
        val vehicleClassHighThreshold = MutableStateFlow<Map<LmuWindowsVehicleClassData, Celsius>>(emptyMap())
        val vehicleClassSelection = MutableStateFlow(LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT)
        val overheatReadoutText = MutableStateFlow(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)
        val coldReadoutText = MutableStateFlow(LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutTyreTemperatureDetailUiState> =
            LmuWindowsAutoReadoutTyreTemperatureDetailFlow(
                enabledStates = enabledStates.tracked(),
                lowWarningPhases = lowWarningPhases.tracked(),
                vehicleClassHighThreshold = vehicleClassHighThreshold.tracked(),
                vehicleClassSelection = vehicleClassSelection.tracked(),
                overheatReadoutText = overheatReadoutText.tracked(),
                coldReadoutText = coldReadoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
