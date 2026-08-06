@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

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
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutTyreTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @MockK
    private lateinit var vehicleClassRepository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

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
        LmuWindowsReadoutTyreTemperatureDetailViewModel(
            observeEnabledStates = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repository),
            observeLowWarningPhases = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
            observeVehicleClassHighThreshold =
                ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(vehicleClassRepository),
            observeVehicleClassSelection =
                ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(vehicleClassRepository),
            saveEnabledState = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository),
            saveLowWarningPhases = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository),
            saveVehicleClassHighThreshold =
                SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(vehicleClassRepository),
            saveVehicleClassSelection =
                SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(vehicleClassRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onOverheatWarningEnabledChangedを呼ぶとuiStateのoverheatWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false) }
            }
            val viewModel = createViewModel()

            viewModel.onOverheatWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().overheatWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onPreviewClickedを呼ぶとTyreOverheatイベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeSelectedVehicleClass() } returns
            MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
        every { ttsEngine.speak(SpeechEvent.TyreOverheat, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
        verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreOverheat, false) }
        confirmVerified(repository, vehicleClassRepository, ttsEngine)
    }

    @Test
    fun `onLowWarningPreviewClickedを呼ぶとTyreColdイベントが再生される`() {
        every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
        every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeSelectedVehicleClass() } returns
            MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
        every { ttsEngine.speak(SpeechEvent.TyreCold, false) } returns Unit
        val viewModel = createViewModel()

        viewModel.onLowWarningPreviewClicked()

        verify(exactly = 1) { repository.observeEnabledStates() }
        verify(exactly = 1) { repository.observeLowWarningPhases() }
        verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
        verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreCold, false) }
        confirmVerified(repository, vehicleClassRepository, ttsEngine)
    }

    @Test
    fun `onLowWarningEnabledChangedを呼ぶとuiStateのlowWarningEnabledが更新される`() =
        runTest {
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeEnabledStates() } returns enabledStatesFlow
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to false) }
            }
            val viewModel = createViewModel()

            viewModel.onLowWarningEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().lowWarningEnabled)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onLowWarningPhaseToggledで未選択のフェーズを渡すと選択に追加される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val lowWarningPhasesFlow =
                MutableStateFlow(
                    mapOf(
                        SessionPhase.GARAGE to false,
                        SessionPhase.WARM_UP to false,
                        SessionPhase.GRID_WALK to false,
                        SessionPhase.FORMATION to false,
                    ),
                )
            every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) } answers {
                lowWarningPhasesFlow.update {
                    mapOf(
                        SessionPhase.GARAGE to true,
                        SessionPhase.WARM_UP to false,
                        SessionPhase.GRID_WALK to false,
                        SessionPhase.FORMATION to false,
                    )
                }
            }
            val viewModel = createViewModel()
            viewModel.uiState.first()

            viewModel.onLowWarningPhaseToggled(SessionPhase.GARAGE)

            assertEquals(setOf(SessionPhase.GARAGE), viewModel.uiState.first().lowWarningPhases)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) { repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onLowWarningPhaseToggledで選択済みのフェーズを渡すと選択から除外される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            val defaultPhases =
                mapOf(
                    SessionPhase.GARAGE to false,
                    SessionPhase.WARM_UP to true,
                    SessionPhase.GRID_WALK to true,
                    SessionPhase.FORMATION to true,
                )
            val lowWarningPhasesFlow = MutableStateFlow(defaultPhases)
            every { repository.observeLowWarningPhases() } returns lowWarningPhasesFlow
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
            } answers {
                lowWarningPhasesFlow.update {
                    mapOf(
                        SessionPhase.GARAGE to false,
                        SessionPhase.WARM_UP to true,
                        SessionPhase.GRID_WALK to true,
                        SessionPhase.FORMATION to false,
                    )
                }
            }
            val viewModel = createViewModel()
            viewModel.uiState.first()

            viewModel.onLowWarningPhaseToggled(SessionPhase.FORMATION)

            assertEquals(
                setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK),
                viewModel.uiState.first().lowWarningPhases,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdChangedを呼ぶとuiStateのvehicleClassHighThresholdCelsiusが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            val vehicleClassHighThresholdFlow =
                MutableStateFlow(mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gte to 95))
            every {
                vehicleClassRepository.observeHighThresholdCelsius()
            } returns vehicleClassHighThresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 100)
            } answers {
                vehicleClassHighThresholdFlow.update { it + (LmuWindowsVehicleClassData.Gte to 100) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdChanged(LmuWindowsVehicleClassData.Gte, 100)

            assertEquals(
                mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gte to 100),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 100)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdResetを呼ぶとそのクラスのしきい値がデフォルト値に戻る`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            val vehicleClassHighThresholdFlow =
                MutableStateFlow(mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gt3 to 100))
            every {
                vehicleClassRepository.observeHighThresholdCelsius()
            } returns vehicleClassHighThresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 90)
            } answers {
                vehicleClassHighThresholdFlow.update { it + (LmuWindowsVehicleClassData.Gt3 to 90) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdReset(LmuWindowsVehicleClassData.Gt3)

            assertEquals(
                mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gt3 to 90),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 90)
            }
            confirmVerified(repository, vehicleClassRepository)
        }

    @Test
    fun `onVehicleClassSelectedを呼ぶとuiStateのselectedVehicleClassが更新される`() =
        runTest {
            every { repository.observeEnabledStates() } returns MutableStateFlow(emptyMap())
            every { repository.observeLowWarningPhases() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            val selectedVehicleClassFlow =
                MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns selectedVehicleClassFlow
            coEvery { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) } answers {
                selectedVehicleClassFlow.value = LmuWindowsVehicleClassData.Gte
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassSelected(LmuWindowsVehicleClassData.Gte)

            assertEquals(LmuWindowsVehicleClassData.Gte, viewModel.uiState.first().selectedVehicleClass)
            verify(exactly = 1) { repository.observeEnabledStates() }
            verify(exactly = 1) { repository.observeLowWarningPhases() }
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            coVerify(exactly = 1) { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(repository, vehicleClassRepository)
        }
}
