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
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.SaveManualReadoutSetsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutListViewModelTest {
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
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    saveManualReadoutSets = SaveManualReadoutSetsUseCase(repository),
                )
            try {
                assertEquals(ManualReadoutListUiState(), viewModel.uiState.value)
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
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    saveManualReadoutSets = SaveManualReadoutSetsUseCase(repository),
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
    fun `addSetは既存セットの末尾に新IDの空セットを追加して保存する`() =
        runTest(dispatcher) {
            val sets = listOf(ManualReadoutSet(id = "set-1", itemKeys = listOf("speed")))
            val expected = sets + ManualReadoutSet(id = "new-set")
            every { repository.observeSets() } returns MutableStateFlow(sets)
            coEvery { repository.saveSets(expected) } returns Unit
            val viewModel =
                ManualReadoutListViewModel(
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(repository),
                    saveManualReadoutSets = SaveManualReadoutSetsUseCase(repository),
                    generateSetId = { "new-set" },
                )
            val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                assertEquals(sets, viewModel.uiState.first { it.sets == sets }.sets)
                viewModel.addSet()
                verify(exactly = 1) { repository.observeSets() }
                coVerify(exactly = 1) { repository.saveSets(expected) }
                confirmVerified(repository)
            } finally {
                collection.cancel()
                viewModel.viewModelScope.cancel()
            }
        }
}
