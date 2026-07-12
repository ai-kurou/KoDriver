package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun createReadoutPreferencesRepository(): ReadoutPreferencesRepository {
    val repository = mockk<ReadoutPreferencesRepository>()
    val enabledStates = MutableStateFlow<Map<String, Map<ReadoutItemKey, Boolean>>>(emptyMap())
    val order = MutableStateFlow<Map<String, List<ReadoutItemKey>>>(emptyMap())
    every { repository.observeReadoutEnabledStates(any()) } answers {
        val simulator = firstArg<String>()
        enabledStates.map { it[simulator] ?: emptyMap() }
    }
    coEvery { repository.saveReadoutEnabledState(any(), any(), any()) } answers {
        val simulator = firstArg<String>()
        val key = secondArg<ReadoutItemKey>()
        val enabled = thirdArg<Boolean>()
        enabledStates.update { all -> all + (simulator to ((all[simulator] ?: emptyMap()) + (key to enabled))) }
    }
    every { repository.observeReadoutOrder(any()) } answers {
        val simulator = firstArg<String>()
        order.map { it[simulator] ?: emptyList() }
    }
    coEvery { repository.saveReadoutOrder(any(), any()) } answers {
        val simulator = firstArg<String>()
        val newOrder = secondArg<List<ReadoutItemKey>>()
        order.update { all -> all + (simulator to newOrder) }
    }
    return repository
}

class ObserveReadoutOrderUseCaseTest {

    @Test
    fun `初期値は空リスト・保存済みの順序を返す・シミュレーターごとに独立している`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        assertTrue(useCase("lmu_windows").first().isEmpty())

        repo.saveReadoutOrder(
            "lmu_windows",
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ),
        )
        repo.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.LmuWindows.Flag.Root))

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ),
            useCase("lmu_windows").first(),
        )
        assertEquals(listOf(ReadoutItemKey.LmuWindows.Flag.Root), useCase("rFactor 2").first())
    }
}
