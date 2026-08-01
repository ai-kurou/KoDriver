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
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutListViewModelQueueTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var queueRepository: QueuePreferencesRepository

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
    fun `キューのデフォルト値にRepositoryの永続化済み状態がマージされて表示される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator?>(null)
            every { queueRepository.observeQueueEnabledStates() } returns
                MutableStateFlow(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to true))
            val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

            val state = viewModel.uiState.first()
            assertEquals(true, state.queueEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
            assertEquals(true, state.queueEnabledStates[ReadoutItemKey.LmuWindows.TyreWear.Root])
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository)
        }

    @Test
    fun `onQueueEnabledChangedでキューのON_OFF状態がRepositoryに保存される`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow<Simulator?>(null)
            val queueEnabledFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { queueRepository.observeQueueEnabledStates() } returns queueEnabledFlow
            coEvery {
                queueRepository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            } answers {
                queueEnabledFlow.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to true) }
            }
            val viewModel = createViewModel(simulatorRepository, readoutRepository, queueRepository)

            viewModel.onQueueEnabledChanged(ReadoutItemKey.LmuWindows.Flag.Root, true)

            assertEquals(true, viewModel.uiState.first().queueEnabledStates[ReadoutItemKey.LmuWindows.Flag.Root])
            coVerify(exactly = 1) {
                queueRepository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { queueRepository.observeQueueEnabledStates() }
            confirmVerified(simulatorRepository, readoutRepository, queueRepository)
        }
}
