@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutVehicleApproachDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: AceWindowsVehicleApproachPreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    private val longitudinalFlow = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT)
    private val lateralFlow = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
    private val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AceWindowsReadoutVehicleApproachDetailViewModel(
            thresholds = AceWindowsVehicleApproachThresholdsUseCases(repository),
            preferences = AceWindowsVehicleApproachPreferencesUseCases(repository),
            observeEnabledStates = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repository),
            saveEnabledState = SaveAceWindowsVehicleApproachEnabledStateUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            val viewModel = createViewModel()

            assertEquals(AceWindowsReadoutVehicleApproachDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            confirmVerified(repository)
        }

    @Test
    fun `onLongitudinalThresholdChangedを呼ぶとuiStateのlongitudinalThresholdMetersが更新される`() =
        runTest {
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery { repository.saveLongitudinalThresholdMeters(7.0) } answers { longitudinalFlow.update { 7.0 } }
            val viewModel = createViewModel()

            viewModel.onLongitudinalThresholdChanged(7.0)

            assertEquals(7.0, viewModel.uiState.first().longitudinalThresholdMeters)
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(7.0) }
            confirmVerified(repository)
        }

    @Test
    fun `onResetLongitudinalThresholdを呼ぶとlongitudinalThresholdMetersがデフォルト値に戻る`() =
        runTest {
            longitudinalFlow.update { 7.0 }
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            val defaultLongitudinal = ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
            coEvery {
                repository.saveLongitudinalThresholdMeters(defaultLongitudinal)
            } answers {
                longitudinalFlow.update { defaultLongitudinal }
            }
            val viewModel = createViewModel()

            viewModel.onResetLongitudinalThreshold()

            assertEquals(
                defaultLongitudinal,
                viewModel.uiState.first().longitudinalThresholdMeters,
            )
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) {
                repository.saveLongitudinalThresholdMeters(defaultLongitudinal)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onLateralThresholdChangedを呼ぶとuiStateのlateralThresholdMetersが更新される`() =
        runTest {
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery { repository.saveLateralThresholdMeters(6.0) } answers { lateralFlow.update { 6.0 } }
            val viewModel = createViewModel()

            viewModel.onLateralThresholdChanged(6.0)

            assertEquals(6.0, viewModel.uiState.first().lateralThresholdMeters)
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) { repository.saveLateralThresholdMeters(6.0) }
            confirmVerified(repository)
        }

    @Test
    fun `onResetLateralThresholdを呼ぶとlateralThresholdMetersがデフォルト値に戻る`() =
        runTest {
            lateralFlow.update { 7.0 }
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery {
                repository.saveLateralThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
            } answers {
                lateralFlow.update { ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onResetLateralThreshold()

            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                viewModel.uiState.first().lateralThresholdMeters,
            )
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) {
                repository.saveLateralThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onStartReadoutEnabledChangedを呼ぶとuiStateのstartReadoutEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false) }
            }
            val viewModel = createViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onStartReadoutTypeChangedを呼ぶとuiStateのstartReadoutTypeが更新されAceWindowsVehicleApproachが再生される`() =
        runTest {
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            } answers { startReadoutTypeFlow.update { VehicleApproachStartReadoutType.CAR_LEFT_RIGHT } }
            every { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.onStartReadoutTypeChanged(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

            assertEquals(
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                viewModel.uiState.first().startReadoutType,
            )
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) { repository.saveStartReadoutType(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT) }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
            confirmVerified(repository, ttsEngine)
        }

    @Test
    fun `onStartReadoutTypeChangedにLEFT_RIGHT_APPROACHを渡してもAceWindowsVehicleApproachが再生される`() =
        runTest {
            every { repository.observeLongitudinalThresholdMeters() } returns longitudinalFlow
            every { repository.observeLateralThresholdMeters() } returns lateralFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeStartReadoutType() } returns startReadoutTypeFlow
            coEvery {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            } answers { startReadoutTypeFlow.update { VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH } }
            every { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) } returns Unit
            val viewModel = createViewModel()

            viewModel.onStartReadoutTypeChanged(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                viewModel.uiState.first().startReadoutType,
            )
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            coVerify(exactly = 1) {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
            confirmVerified(repository, ttsEngine)
        }
}
