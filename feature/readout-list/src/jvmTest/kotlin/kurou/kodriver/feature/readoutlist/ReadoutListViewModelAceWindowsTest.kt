package kurou.kodriver.feature.readoutlist

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
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelAceWindowsTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val simulatorRepository: SimulatorPreferencesRepository = mockk()

    private val readoutRepository: ReadoutPreferencesRepository = mockk()

    private val queueRepository: QueuePreferencesRepository = mockk()

    private val startSoundRepository: ReadoutStartSoundEnabledPreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ace_windowsを選択するとlistPaneにフラッグと車両接近と燃料残り周回数と燃料残量とタイヤ温度と自己ベストラップアイテムが表示される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator>(Simulator.AceWindows)
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("ace_windows") } returns MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("ace_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(
                    simulatorRepository = simulatorRepository,
                    readoutRepository = readoutRepository,
                    queueRepository = queueRepository,
                    startSoundRepository = startSoundRepository,
                )

            val state = viewModel.uiState.first()
            assertEquals(Simulator.AceWindows, state.selectedSimulator)
            assertEquals(
                listOf(
                    AceWindowsReadoutItemKey.Flag.Root,
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                    AceWindowsReadoutItemKey.RemainingFuel.Root,
                    AceWindowsReadoutItemKey.TyreTemperature.Root,
                    AceWindowsReadoutItemKey.MyBestLap.Root,
                ),
                state.items,
            )
            assertEquals(true, state.readoutEnabledStates[AceWindowsReadoutItemKey.Flag.Root])
            assertEquals(true, state.readoutEnabledStates[AceWindowsReadoutItemKey.VehicleApproach.Root])
            assertEquals(true, state.readoutEnabledStates[AceWindowsReadoutItemKey.TyreTemperature.Root])
            assertEquals(true, state.readoutEnabledStates[AceWindowsReadoutItemKey.RemainingFuel.Root])
            assertEquals(true, state.readoutEnabledStates[AceWindowsReadoutItemKey.RemainingFuelLaps.Root])
            assertEquals(false, state.readoutEnabledStates[AceWindowsReadoutItemKey.MyBestLap.Root])
            assertEquals(false, state.queueEnabledStates[AceWindowsReadoutItemKey.Flag.Root])
            assertEquals(null, state.queueEnabledStates[AceWindowsReadoutItemKey.VehicleApproach.Root])
            assertEquals(true, state.queueEnabledStates[AceWindowsReadoutItemKey.TyreTemperature.Root])
            assertEquals(true, state.queueEnabledStates[AceWindowsReadoutItemKey.RemainingFuel.Root])
            assertEquals(true, state.queueEnabledStates[AceWindowsReadoutItemKey.RemainingFuelLaps.Root])
            assertEquals(false, state.queueEnabledStates[AceWindowsReadoutItemKey.MyBestLap.Root])
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("ace_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("ace_windows") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }

    @Test
    fun `ace_windowsの車両接近でonReadoutEnabledChangedするとON_OFF状態がRepositoryに保存される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator>(Simulator.AceWindows)
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("ace_windows") } returns enabledStatesFlow
            every { readoutRepository.observeReadoutOrder("ace_windows") } returns MutableStateFlow(emptyList())
            coEvery {
                readoutRepository.saveReadoutEnabledState(
                    "ace_windows",
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (AceWindowsReadoutItemKey.VehicleApproach.Root to false) }
            }
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel =
                createViewModel(
                    simulatorRepository = simulatorRepository,
                    readoutRepository = readoutRepository,
                    queueRepository = queueRepository,
                    startSoundRepository = startSoundRepository,
                )

            viewModel.onReadoutEnabledChanged(AceWindowsReadoutItemKey.VehicleApproach.Root, false)

            assertEquals(
                false,
                viewModel.uiState.first().readoutEnabledStates[AceWindowsReadoutItemKey.VehicleApproach.Root],
            )
            coVerify(exactly = 1) {
                readoutRepository.saveReadoutEnabledState(
                    "ace_windows",
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
                    false,
                )
            }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("ace_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("ace_windows") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }

    @Test
    fun `ace_windowsの車両接近でonStartSoundEnabledChangedすると読み上げ開始音のON_OFF状態がRepositoryに保存される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator>(Simulator.AceWindows)
            every { readoutRepository.observeReadoutEnabledStates("ace_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("ace_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val startSoundEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns startSoundEnabledFlow
            coEvery {
                startSoundRepository.saveStartSoundEnabledState(
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
                    true,
                )
            } answers {
                startSoundEnabledFlow.update { it + (AceWindowsReadoutItemKey.VehicleApproach.Root to true) }
            }
            val viewModel =
                createViewModel(
                    simulatorRepository = simulatorRepository,
                    readoutRepository = readoutRepository,
                    queueRepository = queueRepository,
                    startSoundRepository = startSoundRepository,
                )

            viewModel.onStartSoundEnabledChanged(AceWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertEquals(
                true,
                viewModel.uiState.first().startSoundEnabledStates[AceWindowsReadoutItemKey.VehicleApproach.Root],
            )
            coVerify(exactly = 1) {
                startSoundRepository.saveStartSoundEnabledState(
                    AceWindowsReadoutItemKey.VehicleApproach.Root,
                    true,
                )
            }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("ace_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("ace_windows") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }
}
