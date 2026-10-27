package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutBrakeTemperatureDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映して設定と文言とTTS可否を保持する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutBrakeTemperatureDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutBrakeTemperatureDetailUiState()
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout to false,
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout to true,
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root to false,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = true)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { emptyMap() }
            testScheduler.runCurrent()
            assertEquals(expected, states.last())

            inputs.vehicleClassHighThreshold.update {
                mapOf(
                    LmuWindowsVehicleClassData.Hypercar to 900,
                    LmuWindowsVehicleClassData.P2 to 850,
                )
            }
            testScheduler.runCurrent()
            expected =
                expected.copy(
                    vehicleClassHighThresholdCelsius =
                        mapOf(LmuWindowsVehicleClassData.Hypercar to 900, LmuWindowsVehicleClassData.P2 to 850),
                )
            assertEquals(expected, states.last())

            inputs.vehicleClassSelection.update { LmuWindowsVehicleClassData.P2 }
            testScheduler.runCurrent()
            expected = expected.copy(selectedVehicleClass = LmuWindowsVehicleClassData.P2)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "過熱 {celsius}度" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "過熱 {celsius}度")
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
            assertEquals(5, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(10, inputs.activeSubscriptions)

            firstCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(5, inputs.activeSubscriptions)

            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.readoutText.update { "再購読" }
            assertEquals(LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(readoutText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val vehicleClassHighThreshold = MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(emptyMap())
        val vehicleClassSelection = MutableStateFlow(LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT)
        val readoutText = MutableStateFlow(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutBrakeTemperatureDetailUiState> =
            LmuWindowsAutoReadoutBrakeTemperatureDetailFlow(
                enabledStates = enabledStates.tracked(),
                vehicleClassHighThreshold = vehicleClassHighThreshold.tracked(),
                vehicleClassSelection = vehicleClassSelection.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
