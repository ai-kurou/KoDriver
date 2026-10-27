package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutVehicleApproachDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()

            assertEquals(LmuWindowsAutoReadoutVehicleApproachDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各入力の更新を反映して設定と文言とTTS可否を保持する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutVehicleApproachDetailUiState>()
            backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutVehicleApproachDetailUiState()
            assertEquals(expected, states.last())

            inputs.lateralThresholdMeters.update { 8.5 }
            testScheduler.runCurrent()
            expected = expected.copy(lateralThresholdMeters = 8.5)
            assertEquals(expected, states.last())

            inputs.longitudinalThresholdMeters.update { 12.0 }
            testScheduler.runCurrent()
            expected = expected.copy(longitudinalThresholdMeters = 12.0)
            assertEquals(expected, states.last())

            inputs.sustainedApproachDurationSeconds.update { 7 }
            testScheduler.runCurrent()
            expected = expected.copy(sustainedApproachDurationSeconds = 7)
            assertEquals(expected, states.last())

            inputs.skipFirstLap.update { false }
            testScheduler.runCurrent()
            expected = expected.copy(skipFirstLap = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update {
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to true,
                    LmuWindowsReadoutItemKey.VehicleApproach.Root to false,
                )
            }
            testScheduler.runCurrent()
            expected = expected.copy(startReadoutEnabled = false, sustainedReadoutEnabled = true)
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(startLeftText = "左開始") }
            testScheduler.runCurrent()
            expected = expected.copy(startLeftText = "左開始")
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(startRightText = "") }
            testScheduler.runCurrent()
            expected = expected.copy(startRightText = "")
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(sustainedLeftText = "左継続") }
            testScheduler.runCurrent()
            expected = expected.copy(sustainedLeftText = "左継続")
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(sustainedRightText = "右継続") }
            testScheduler.runCurrent()
            expected = expected.copy(sustainedRightText = "右継続")
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(isTextToSpeechAvailable = true) }
            testScheduler.runCurrent()
            expected = expected.copy(isTextToSpeechAvailable = true)
            assertEquals(expected, states.last())

            inputs.readoutTextSettings.update { it.copy(isTextToSpeechAvailable = false) }
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
            assertEquals(6, inputs.activeSubscriptions)

            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(12, inputs.activeSubscriptions)

            firstCollector.cancel()
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)

            inputs.readoutTextSettings.update { it.copy(startLeftText = "再購読") }
            assertEquals(LmuWindowsAutoReadoutVehicleApproachDetailUiState(startLeftText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    private class Inputs {
        val lateralThresholdMeters = MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
        val longitudinalThresholdMeters =
            MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT)
        val sustainedApproachDurationSeconds =
            MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT)
        val skipFirstLap = MutableStateFlow(true)
        val enabledStates =
            MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false,
                ),
            )
        val readoutTextSettings = MutableStateFlow(ReadoutTextSettingsUiState())
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutVehicleApproachDetailUiState> =
            LmuWindowsAutoReadoutVehicleApproachDetailFlow(
                lateralThresholdMeters = lateralThresholdMeters.tracked(),
                longitudinalThresholdMeters = longitudinalThresholdMeters.tracked(),
                sustainedApproachDurationSeconds = sustainedApproachDurationSeconds.tracked(),
                skipFirstLap = skipFirstLap.tracked(),
                enabledStates = enabledStates.tracked(),
                readoutTextSettings = readoutTextSettings.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
