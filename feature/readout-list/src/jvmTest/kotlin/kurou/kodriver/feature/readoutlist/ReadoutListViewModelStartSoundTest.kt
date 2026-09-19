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
class ReadoutListViewModelStartSoundTest {
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
    fun `読み上げ開始音のデフォルト値にRepositoryの永続化済み状態がマージされて表示される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator>(Simulator.LmuWindows)
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns
                MutableStateFlow(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to false))
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            val state = viewModel.uiState.first()
            assertEquals(false, state.startSoundEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
            assertEquals(false, state.startSoundEnabledStates[ReadoutItemKey.LmuWindows.VehicleApproach.Root])
            assertEquals(true, state.startSoundEnabledStates[ReadoutItemKey.LmuWindows.TyreWear.Root])
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("lmu_windows") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }

    @Test
    fun `onStartSoundEnabledChangedで読み上げ開始音のON_OFF状態がRepositoryに保存される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator>(Simulator.LmuWindows)
            every { readoutRepository.observeReadoutEnabledStates("lmu_windows") } returns
                MutableStateFlow(emptyMap())
            every { readoutRepository.observeReadoutOrder("lmu_windows") } returns MutableStateFlow(emptyList())
            every { queueRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val startSoundEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { startSoundRepository.observeStartSoundEnabledStates() } returns startSoundEnabledFlow
            coEvery {
                startSoundRepository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)
            } answers {
                startSoundEnabledFlow.update { it + (ReadoutItemKey.LmuWindows.VehicleApproach.Root to true) }
            }
            val viewModel =
                createViewModel(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)

            viewModel.onStartSoundEnabledChanged(ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)

            assertEquals(
                true,
                viewModel.uiState.first().startSoundEnabledStates[ReadoutItemKey.LmuWindows.VehicleApproach.Root],
            )
            coVerify(exactly = 1) {
                startSoundRepository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)
            }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { readoutRepository.observeReadoutEnabledStates("lmu_windows") }
            verify(exactly = 1) { readoutRepository.observeReadoutOrder("lmu_windows") }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            verify(exactly = 1) { startSoundRepository.observeStartSoundEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository, startSoundRepository)
        }
}
