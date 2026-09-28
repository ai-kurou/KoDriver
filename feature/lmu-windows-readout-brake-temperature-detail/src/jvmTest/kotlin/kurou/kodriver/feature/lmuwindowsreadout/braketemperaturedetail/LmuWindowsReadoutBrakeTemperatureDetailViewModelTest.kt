@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeTemperatureDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val vehicleClassRepository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk()

    private val readoutPreferencesRepository: ReadoutPreferencesRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) } returns
            enabledStatesFlow
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutBrakeTemperatureDetailViewModel(
            brakeTemperatureUseCases =
                BrakeTemperatureUseCases(
                    observeVehicleClassHighThreshold =
                        ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(vehicleClassRepository),
                    observeVehicleClassSelection =
                        ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(vehicleClassRepository),
                    saveVehicleClassHighThreshold =
                        SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(vehicleClassRepository),
                    saveVehicleClassSelection =
                        SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(vehicleClassRepository),
                ),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutPreferencesRepository),
            playSpeechEvent = PlaySpeechEventUseCase(ttsEngine),
        )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() =
        runTest {
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns
                MutableStateFlow(mapOf(LmuWindowsVehicleClassData.Gte to 700))
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutBrakeTemperatureDetailUiState(
                    vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Gte to 700),
                    selectedVehicleClass = LmuWindowsVehicleClassData.Hypercar,
                    enabled = true,
                ),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdChangedを呼ぶとuiStateのvehicleClassHighThresholdCelsiusが更新される`() =
        runTest {
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gte to 700))
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gte to 800) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdChanged(LmuWindowsVehicleClassData.Gte, 800)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 800),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassHighThresholdResetを呼ぶとそのクラスの閾値がデフォルト値に戻る`() =
        runTest {
            val thresholdFlow =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(mapOf(LmuWindowsVehicleClassData.Gt3 to 600))
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns thresholdFlow
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 800)
            } answers {
                thresholdFlow.update { it + (LmuWindowsVehicleClassData.Gt3 to 800) }
            }
            val viewModel = createViewModel()

            viewModel.onVehicleClassHighThresholdReset(LmuWindowsVehicleClassData.Gt3)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gt3 to 800),
                viewModel.uiState.first().vehicleClassHighThresholdCelsius,
            )
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                vehicleClassRepository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 800)
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onVehicleClassSelectedを呼ぶとuiStateのselectedVehicleClassが更新される`() =
        runTest {
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
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) { vehicleClassRepository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }

    @Test
    fun `onWarningChipClickedを呼ぶとBrakeOverheatイベントが再生される`() {
        every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
        every { vehicleClassRepository.observeSelectedVehicleClass() } returns
            MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
        val viewModel = createViewModel()

        viewModel.onWarningChipClicked()

        verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
        verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
        verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BrakeOverheat, false) }
        confirmVerified(vehicleClassRepository, readoutPreferencesRepository, ttsEngine)
    }

    @Test
    fun `onEnabledChangedにfalseを渡すとuiStateのenabledがfalseになる`() =
        runTest {
            every { vehicleClassRepository.observeHighThresholdCelsius() } returns MutableStateFlow(emptyMap())
            every { vehicleClassRepository.observeSelectedVehicleClass() } returns
                MutableStateFlow(LmuWindowsVehicleClassData.Hypercar)
            coEvery {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    ReadoutItemKey.LmuWindows.BrakeTemperature.WarningReadout,
                    false,
                )
            } answers {
                enabledStatesFlow.update {
                    it + (ReadoutItemKey.LmuWindows.BrakeTemperature.WarningReadout to false)
                }
            }
            val viewModel = createViewModel()

            viewModel.onEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().enabled)
            verify(exactly = 1) { vehicleClassRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { vehicleClassRepository.observeSelectedVehicleClass() }
            verify(exactly = 1) { readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.LmuWindows.id) }
            coVerify(exactly = 1) {
                readoutPreferencesRepository.saveReadoutEnabledState(
                    Simulator.LmuWindows.id,
                    ReadoutItemKey.LmuWindows.BrakeTemperature.WarningReadout,
                    false,
                )
            }
            confirmVerified(vehicleClassRepository, readoutPreferencesRepository)
        }
}
