package kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsAutoReadoutVehicleApproachDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(AceWindowsAutoReadoutVehicleApproachDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映して設定と文言とTTS可否を保持する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<AceWindowsAutoReadoutVehicleApproachDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = AceWindowsAutoReadoutVehicleApproachDetailUiState()
            assertEquals(expected, states.last())

            inputs.thresholdMeters.update { 8.5 }
            testScheduler.runCurrent()
            expected = expected.copy(thresholdMeters = 8.5)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    AceWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                    AceWindowsReadoutItemKey.VehicleApproach.Root to true,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(startReadoutEnabled = false)
            assertEquals(expected, states.last())

            inputs.readoutText.update { "周囲に注意" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "周囲に注意")
            assertEquals(expected, states.last())

            inputs.readoutText.update { "" }
            testScheduler.runCurrent()
            expected = expected.copy(readoutText = "")
            assertEquals(expected, states.last())

            inputs.isTextToSpeechAvailable.update { true }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
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
            assertEquals(4, inputs.activeSubscriptions)

            val states = mutableListOf<AceWindowsAutoReadoutVehicleApproachDetailUiState>()
            val secondCollector = backgroundScope.launch { flow.collect { states.add(it) } }
            testScheduler.runCurrent()
            assertEquals(8, inputs.activeSubscriptions)

            firstCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(4, inputs.activeSubscriptions)

            inputs.readoutText.update { "購読継続" }
            testScheduler.runCurrent()
            assertEquals(AceWindowsAutoReadoutVehicleApproachDetailUiState(readoutText = "購読継続"), states.last())

            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.readoutText.update { "再購読" }
            assertEquals(AceWindowsAutoReadoutVehicleApproachDetailUiState(readoutText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val thresholdMeters = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
        val enabledStates =
            MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                mapOf(
                    AceWindowsReadoutItemKey.VehicleApproach.StartReadout to
                        ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT,
                ),
            )
        val readoutText = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT)
        val isTextToSpeechAvailable = MutableStateFlow(false)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<AceWindowsAutoReadoutVehicleApproachDetailUiState> =
            AceWindowsAutoReadoutVehicleApproachDetailFlow(
                thresholdMeters = thresholdMeters.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutText = readoutText.tracked(),
                isTextToSpeechAvailable = isTextToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
