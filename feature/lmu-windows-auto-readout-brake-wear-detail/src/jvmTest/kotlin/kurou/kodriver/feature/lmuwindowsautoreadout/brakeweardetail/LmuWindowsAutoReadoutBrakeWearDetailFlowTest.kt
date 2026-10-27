package kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutBrakeWearDetailFlowTest {
    @Test
    fun `既定の入力と空の有効状態から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutBrakeWearDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映し警告キーだけで読み上げ有効状態を切り替える`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutBrakeWearDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutBrakeWearDetailUiState()
            assertEquals(expected, states.last())

            inputs.vehicleClassLowThreshold.update {
                mapOf(LmuWindowsVehicleClassData.Hypercar to 15, LmuWindowsVehicleClassData.Gte to 25)
            }
            testScheduler.runCurrent()
            expected =
                expected.copy(
                    vehicleClassLowThresholdPercent =
                        mapOf(LmuWindowsVehicleClassData.Hypercar to 15, LmuWindowsVehicleClassData.Gte to 25),
                )
            assertEquals(expected, states.last())

            inputs.vehicleClassSelection.update { LmuWindowsVehicleClassData.Gte }
            testScheduler.runCurrent()
            expected = expected.copy(selectedVehicleClass = LmuWindowsVehicleClassData.Gte)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "残量{percent}%" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "残量{percent}%")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.BrakeWear.WarningReadout to false,
                    LmuWindowsReadoutItemKey.BrakeWear.Root to true,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.BrakeWear.WarningReadout to true,
                    LmuWindowsReadoutItemKey.BrakeWear.Root to false,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(enabled = true)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to false) }
            testScheduler.runCurrent()
            assertEquals(expected, states.last())

            inputs.vehicleClassLowThreshold.update { emptyMap() }
            testScheduler.runCurrent()
            expected = expected.copy(vehicleClassLowThresholdPercent = emptyMap())
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { false }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = false)
            assertEquals(expected, states.last())
        }

    @Test
    fun `購読ごとに五つの上流を購読し解除後は最新入力で再購読できる`() =
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
            assertEquals(LmuWindowsAutoReadoutBrakeWearDetailUiState(readoutText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val vehicleClassLowThreshold = MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(emptyMap())
        val vehicleClassSelection = MutableStateFlow(LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT)
        val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        val readoutText = MutableStateFlow(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutBrakeWearDetailUiState> =
            LmuWindowsAutoReadoutBrakeWearDetailFlow(
                vehicleClassLowThreshold = vehicleClassLowThreshold.tracked(),
                vehicleClassSelection = vehicleClassSelection.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
