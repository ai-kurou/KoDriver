package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutListViewModelTest {
    private val requestRepository: ManualReadoutRequestRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態と保存済みセットが0件の状態は空一覧を返す`() =
        runTest(dispatcher) {
            every { repository.observeSets() } returns MutableStateFlow(emptyList())
            val viewModel =
                ManualReadoutListViewModel(
                    simulatorId = Simulator.LmuWindows.id,
                    requestManualReadout = RequestManualReadoutUseCase(requestRepository),
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    addManualReadoutSet = AddManualReadoutSetUseCase(repository),
                )
            try {
                assertEquals(ManualReadoutListUiState(canReadSets = true), viewModel.uiState.value)
                assertEquals(emptyList(), viewModel.uiState.first().sets)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `保存済みセットと購読中の変更をUiStateに反映する`() =
        runTest(dispatcher) {
            val sets = listOf(ManualReadoutSet(id = "set-1", itemKeys = listOf("speed")))
            val setsFlow = MutableStateFlow(sets)
            every { repository.observeSets() } returns setsFlow
            val viewModel =
                ManualReadoutListViewModel(
                    simulatorId = Simulator.LmuWindows.id,
                    requestManualReadout = RequestManualReadoutUseCase(requestRepository),
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    addManualReadoutSet = AddManualReadoutSetUseCase(repository),
                )
            val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                assertEquals(sets, viewModel.uiState.first { it.sets == sets }.sets)
                setsFlow.update { emptyList() }
                assertEquals(emptyList(), viewModel.uiState.first().sets)
            } finally {
                collection.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `初回読み込み前の連続追加は一覧状態を使わず各セットを渡す`() =
        runTest(dispatcher) {
            val sets = listOf(ManualReadoutSet(id = "existing", itemKeys = listOf("SESSION")))
            val first = ManualReadoutSet(id = "first")
            val second = ManualReadoutSet(id = "second")
            val ids = listOf(first.id, second.id).iterator()
            every { repository.observeSets() } returns MutableStateFlow(sets)
            coEvery { repository.addSet(first) } returns Unit
            coEvery { repository.addSet(second) } returns Unit
            val viewModel =
                ManualReadoutListViewModel(
                    simulatorId = Simulator.LmuWindows.id,
                    requestManualReadout = RequestManualReadoutUseCase(requestRepository),
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    addManualReadoutSet = AddManualReadoutSetUseCase(repository),
                    generateSetId = { ids.next() },
                )
            try {
                assertEquals(emptyList(), viewModel.uiState.value.sets)
                viewModel.addSet()
                viewModel.addSet()
                assertEquals(emptyList(), viewModel.uiState.value.sets)
                verify(exactly = 1) { repository.observeSets() }
                coVerify(exactly = 1) { repository.addSet(first) }
                coVerify(exactly = 1) { repository.addSet(second) }
                confirmVerified(repository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `一覧への反映前に連続追加しても新IDの空セットだけをRepositoryに渡す`() =
        runTest(dispatcher) {
            val sets = listOf(ManualReadoutSet(id = "set-1", itemKeys = listOf("speed")))
            val expected = ManualReadoutSet(id = "new-set")
            val second = ManualReadoutSet(id = "second-set")
            val ids = listOf(expected.id, second.id).iterator()
            every { repository.observeSets() } returns MutableStateFlow(sets)
            coEvery { repository.addSet(expected) } returns Unit
            coEvery { repository.addSet(second) } returns Unit
            val viewModel =
                ManualReadoutListViewModel(
                    simulatorId = Simulator.LmuWindows.id,
                    requestManualReadout = RequestManualReadoutUseCase(requestRepository),
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    addManualReadoutSet = AddManualReadoutSetUseCase(repository),
                    generateSetId = { ids.next() },
                )
            val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                assertEquals(sets, viewModel.uiState.first { it.sets == sets }.sets)
                viewModel.addSet()
                verify(exactly = 1) { repository.observeSets() }
                viewModel.addSet()
                assertEquals(sets, viewModel.uiState.value.sets)
                coVerify(exactly = 1) { repository.addSet(expected) }
                coVerify(exactly = 1) { repository.addSet(second) }
                confirmVerified(repository)
            } finally {
                collection.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `全シミュレーターの初期状態と購読状態はLMUのみ読み上げ可能で依頼に各IDを渡す`() =
        runTest(dispatcher) {
            every { repository.observeSets() } returns MutableStateFlow(emptyList())
            Simulator.entries.forEach { simulator ->
                val request = ManualReadoutRequest(simulator.id, "set-1")
                coEvery { requestRepository.request(request) } returns Unit
                val viewModel =
                    ManualReadoutListViewModel(
                        simulatorId = simulator.id,
                        requestManualReadout = RequestManualReadoutUseCase(requestRepository),
                        observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                        addManualReadoutSet = AddManualReadoutSetUseCase(repository),
                    )
                val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
                try {
                    val expected = ManualReadoutListUiState(canReadSets = simulator == Simulator.LmuWindows)
                    assertEquals(expected, viewModel.uiState.value)
                    assertEquals(expected, viewModel.uiState.first())
                    viewModel.readSet("set-1")
                    coVerify(exactly = 1) { requestRepository.request(request) }
                } finally {
                    collection.cancel()
                    viewModel.viewModelScope.cancel()
                }
            }
            confirmVerified(requestRepository)
        }
}
