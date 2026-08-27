package kurou.kodriver.feature.readoutlist

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

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var queueRepository: QueuePreferencesRepository

    @MockK
    private lateinit var startSoundRepository: ReadoutStartSoundEnabledPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ace_windowsを選択するとlistPaneにフラッグと車両接近とタイヤ温度と燃料残量アイテムが表示される`() =
        runTest {
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
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

            simulatorFlow.update { Simulator.AceWindows }

            val state = viewModel.uiState.first()
            assertEquals(Simulator.AceWindows, state.selectedSimulator)
            assertEquals(
                listOf(
                    ReadoutItemKey.AceWindows.Flag.Root,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root,
                    ReadoutItemKey.AceWindows.RemainingFuel.Root,
                ),
                state.items,
            )
            assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.Flag.Root])
            assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.VehicleApproach.Root])
            assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.TyreTemperature.Root])
            assertEquals(true, state.readoutEnabledStates[ReadoutItemKey.AceWindows.RemainingFuel.Root])
            assertEquals(false, state.queueEnabledStates[ReadoutItemKey.AceWindows.Flag.Root])
            assertEquals(null, state.queueEnabledStates[ReadoutItemKey.AceWindows.VehicleApproach.Root])
            assertEquals(true, state.queueEnabledStates[ReadoutItemKey.AceWindows.TyreTemperature.Root])
            assertEquals(true, state.queueEnabledStates[ReadoutItemKey.AceWindows.RemainingFuel.Root])
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
            val simulatorFlow = MutableStateFlow<Simulator?>(null)
            val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { simulatorRepository.selectedSimulator() } returns simulatorFlow
            every { readoutRepository.observeReadoutEnabledStates("ace_windows") } returns enabledStatesFlow
            every { readoutRepository.observeReadoutOrder("ace_windows") } returns MutableStateFlow(emptyList())
            coEvery {
                readoutRepository.saveReadoutEnabledState(
                    "ace_windows",
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    false,
                )
            } answers {
                enabledStatesFlow.update { it + (ReadoutItemKey.AceWindows.VehicleApproach.Root to false) }
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

            simulatorFlow.update { Simulator.AceWindows }
            viewModel.onReadoutEnabledChanged(ReadoutItemKey.AceWindows.VehicleApproach.Root, false)

            assertEquals(
                false,
                viewModel.uiState.first().readoutEnabledStates[ReadoutItemKey.AceWindows.VehicleApproach.Root],
            )
            coVerify(exactly = 1) {
                readoutRepository.saveReadoutEnabledState(
                    "ace_windows",
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
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
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator?>(null)
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val startSoundEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns startSoundEnabledFlow
            coEvery {
                startSoundRepository.saveStartSoundEnabledState(
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    true,
                )
            } answers {
                startSoundEnabledFlow.update { it + (ReadoutItemKey.AceWindows.VehicleApproach.Root to true) }
            }
            val viewModel =
                createViewModel(
                    simulatorRepository = simulatorRepository,
                    readoutRepository = readoutRepository,
                    queueRepository = queueRepository,
                    startSoundRepository = startSoundRepository,
                )

            viewModel.onStartSoundEnabledChanged(ReadoutItemKey.AceWindows.VehicleApproach.Root, true)

            assertEquals(
                true,
                viewModel.uiState.first().startSoundEnabledStates[ReadoutItemKey.AceWindows.VehicleApproach.Root],
            )
            coVerify(exactly = 1) {
                startSoundRepository.saveStartSoundEnabledState(
                    ReadoutItemKey.AceWindows.VehicleApproach.Root,
                    true,
                )
            }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }
}
