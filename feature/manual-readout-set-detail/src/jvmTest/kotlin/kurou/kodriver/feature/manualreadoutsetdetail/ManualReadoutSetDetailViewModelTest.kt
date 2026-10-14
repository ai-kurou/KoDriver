package kurou.kodriver.feature.manualreadoutsetdetail

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.UpdateManualReadoutSetUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetDetailViewModelTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(simulatorId: String = Simulator.LmuWindows.id) =
        ManualReadoutSetDetailViewModel(
            simulatorId,
            "target",
            ObserveManualReadoutSetsUseCase(repository),
            UpdateManualReadoutSetUseCase(repository),
            DeleteManualReadoutSetUseCase(repository),
        )

    @Test
    fun `連続選択は最新の保存状態を使い未知キーを保持し削除完了を通知する`() =
        runTest(dispatcher) {
            val key = manualReadoutItemKeys(Simulator.LmuWindows.id).single()
            val original = ManualReadoutSet("target", listOf("UNKNOWN"))
            val selected = original.copy(itemKeys = original.itemKeys + key)
            val other = ManualReadoutSet("other")
            val sets = MutableStateFlow(listOf(other, original))
            every { repository.observeSets() } returns sets
            coEvery { repository.updateSet(selected) } answers { sets.value = listOf(other, selected) }
            coEvery { repository.updateSet(original) } answers { sets.value = listOf(other, original) }
            coEvery { repository.deleteSet("target") } answers { sets.value = listOf(other) }
            val vm = viewModel()
            val collection = backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
            try {
                assertEquals(2, vm.uiState.first { !it.isLoading }.setNumber)
                vm.toggleItem("UNKNOWN")
                vm.toggleItem(key)
                assertEquals(selected.itemKeys, vm.uiState.first().selectedItemKeys)
                vm.toggleItem(key)
                assertEquals(original.itemKeys, vm.uiState.first().selectedItemKeys)
                sets.value = listOf(original, other)
                assertEquals(1, vm.uiState.first().setNumber)
                vm.deleteSet()
                vm.deleteSet()
                vm.toggleItem(key)
                assertTrue(vm.uiState.first { it.deleted }.deleted)
                assertEquals(null, vm.uiState.first().setNumber)
                verify(exactly = 3) { repository.observeSets() }
                coVerify(exactly = 1) { repository.updateSet(selected) }
                coVerify(exactly = 1) { repository.updateSet(original) }
                coVerify(exactly = 1) { repository.deleteSet("target") }
                confirmVerified(repository)
            } finally {
                collection.cancel()
                vm.viewModelScope.cancel()
            }
        }

    @Test
    fun `初期読込と存在しないセットと未対応シミュレーターの項目を返す`() =
        runTest(dispatcher) {
            every { repository.observeSets() } returns MutableStateFlow(emptyList())
            Simulator.entries.forEach { simulator ->
                val vm = viewModel(simulator.id)
                try {
                    assertTrue(vm.uiState.value.isLoading)
                    val state = vm.uiState.first { !it.isLoading }
                    assertEquals(null, state.setNumber)
                    assertEquals(manualReadoutItemKeys(simulator.id), state.availableItemKeys)
                    assertFalse(state.deleted)
                    vm.toggleItem(manualReadoutItemKeys(Simulator.LmuWindows.id).single())
                } finally {
                    vm.viewModelScope.cancel()
                }
            }
            verify(exactly = 4) { repository.observeSets() }
            confirmVerified(repository)
        }
}
