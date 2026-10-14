package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun `文言は入力ごとに正規化して保存し選択解除と再選択でも未知文言を保持する`() =
        runTest(dispatcher) {
            val key = manualReadoutItemKeys(Simulator.LmuWindows.id).single()
            val original = ManualReadoutSet("target", listOf(key), mapOf("UNKNOWN" to "保持"))
            val edited = original.copy(itemTexts = original.itemTexts + (key to "残量{percent}%"))
            val blank = edited.copy(itemTexts = edited.itemTexts + (key to ""))
            val unselected = blank.copy(itemKeys = emptyList())
            val longText = "あ".repeat(30)
            val limited = blank.copy(itemTexts = blank.itemTexts + (key to longText))
            val sets = MutableStateFlow(listOf(original))
            every { repository.observeSets() } returns sets
            listOf(edited, blank, unselected, limited).forEach { updated ->
                coEvery { repository.updateSet(updated) } answers { sets.value = listOf(updated) }
            }
            coEvery { repository.deleteSet("target") } returns Unit
            val vm = viewModel()
            val collection = backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
            try {
                assertEquals(original.itemTexts, vm.uiState.first { !it.isLoading }.itemTexts)
                vm.updateItemText("UNKNOWN", "変更不可")
                vm.updateItemText(key, "  残量{percent}%  ")
                assertEquals(edited.itemTexts, vm.uiState.value.itemTexts)
                vm.updateItemText(key, "  ")
                assertEquals(blank.itemTexts, vm.uiState.value.itemTexts)
                vm.toggleItem(key)
                vm.toggleItem(key)
                assertEquals(blank.itemTexts, vm.uiState.value.itemTexts)
                vm.updateItemText(key, longText + "超過")
                assertEquals(limited.itemTexts, vm.uiState.value.itemTexts)
                vm.deleteSet()
                vm.updateItemText(key, "削除後は保存しない")
                verify(exactly = 6) { repository.observeSets() }
                coVerify(exactly = 1) { repository.updateSet(edited) }
                coVerify(exactly = 2) { repository.updateSet(blank) }
                coVerify(exactly = 1) { repository.updateSet(unselected) }
                coVerify(exactly = 1) { repository.updateSet(limited) }
                coVerify(exactly = 1) { repository.deleteSet("target") }
                confirmVerified(repository)
            } finally {
                collection.cancel()
                vm.viewModelScope.cancel()
            }
        }

    @Test
    fun `存在しないセットと未対応項目の文言は保存しない`() =
        runTest(dispatcher) {
            every { repository.observeSets() } returns MutableStateFlow(emptyList())
            val vm = viewModel()
            try {
                vm.updateItemText("UNKNOWN", "文言")
                vm.updateItemText(manualReadoutItemKeys(Simulator.LmuWindows.id).single(), "文言")
                verify(exactly = 2) { repository.observeSets() }
                confirmVerified(repository)
            } finally {
                vm.viewModelScope.cancel()
            }
        }

    @Test
    fun `保存待ち中の選択変更と連続編集はMutexで順序を保ち直前の結果を使う`() =
        runTest(dispatcher) {
            val key = manualReadoutItemKeys(Simulator.LmuWindows.id).single()
            val original = ManualReadoutSet("target", listOf(key))
            val edited = original.copy(itemTexts = mapOf(key to "残量{percent}%"))
            val unselected = edited.copy(itemKeys = emptyList())
            val next = unselected.copy(itemTexts = mapOf(key to "残り{percent}%"))
            val sets = MutableStateFlow(listOf(original))
            val releaseSave = CompletableDeferred<Unit>()
            every { repository.observeSets() } returns sets
            coEvery { repository.updateSet(edited) } coAnswers {
                releaseSave.await()
                sets.value = listOf(edited)
            }
            coEvery { repository.updateSet(unselected) } answers { sets.value = listOf(unselected) }
            coEvery { repository.updateSet(next) } answers { sets.value = listOf(next) }
            val vm = viewModel()
            try {
                vm.updateItemText(key, "残量{percent}%")
                vm.toggleItem(key)
                vm.updateItemText(key, "残り{percent}%")
                assertEquals(listOf(original), sets.value)
                releaseSave.complete(Unit)
                runCurrent()
                assertEquals(listOf(next), sets.value)
                verify(exactly = 4) { repository.observeSets() }
                coVerify(exactly = 1) { repository.updateSet(edited) }
                coVerify(exactly = 1) { repository.updateSet(unselected) }
                coVerify(exactly = 1) { repository.updateSet(next) }
                confirmVerified(repository)
            } finally {
                vm.viewModelScope.cancel()
            }
        }
}
