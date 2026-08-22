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
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
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

    private val thresholdFlow = MutableStateFlow(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)

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
            observeEnabledStates = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repository),
            saveEnabledState = SaveAceWindowsVehicleApproachEnabledStateUseCase(repository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はデフォルト値のUiStateを返す`() =
        runTest {
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel = createViewModel()

            assertEquals(AceWindowsReadoutVehicleApproachDetailUiState(), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdMetersが更新される`() =
        runTest {
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            coEvery { repository.saveThresholdMeters(7.0) } answers { thresholdFlow.update { 7.0 } }
            val viewModel = createViewModel()

            viewModel.onThresholdChanged(7.0)

            assertEquals(7.0, viewModel.uiState.first().thresholdMeters)
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) { repository.saveThresholdMeters(7.0) }
            confirmVerified(repository)
        }

    @Test
    fun `onResetThresholdを呼ぶとthresholdMetersがデフォルト値に戻る`() =
        runTest {
            thresholdFlow.update { 7.0 }
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            coEvery {
                repository.saveThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
            } answers {
                thresholdFlow.update { ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT }
            }
            val viewModel = createViewModel()

            viewModel.onResetThreshold()

            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
                viewModel.uiState.first().thresholdMeters,
            )
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onStartReadoutEnabledChangedを呼ぶとuiStateのstartReadoutEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeThresholdMeters() } returns thresholdFlow
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false) }
            }
            val viewModel = createViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
            verify(exactly = 1) { repository.observeThresholdMeters() }
            verify(exactly = 1) { repository.observeEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            }
            confirmVerified(repository)
        }

    @Test
    fun `onPreviewClickedを呼ぶとAceWindowsVehicleApproachイベントが再生される`() {
        every { repository.observeThresholdMeters() } returns thresholdFlow
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeThresholdMeters() }
        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
        confirmVerified(repository, ttsEngine)
    }
}
