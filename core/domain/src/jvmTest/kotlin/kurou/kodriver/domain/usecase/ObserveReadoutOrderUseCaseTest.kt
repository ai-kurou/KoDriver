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
    listOf("lmu_windows", "gt7_ps5", "rFactor 2").forEach { simulator ->
        every { repository.observeReadoutEnabledStates(simulator) } answers {
            enabledStates.map { it[simulator] ?: emptyMap() }
        }
        every { repository.observeReadoutOrder(simulator) } answers {
            order.map { it[simulator] ?: emptyList() }
        }
    }
    listOf(
        Triple("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true),
        Triple("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, false),
        Triple("rFactor 2", ReadoutItemKey.LmuWindows.VehicleApproach.Root, false),
    ).forEach { (simulator, key, enabled) ->
        coEvery { repository.saveReadoutEnabledState(simulator, key, enabled) } answers {
            enabledStates.update { all -> all + (simulator to ((all[simulator] ?: emptyMap()) + (key to enabled))) }
        }
    }
    listOf(
        "lmu_windows" to listOf(
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
            ReadoutItemKey.LmuWindows.Flag.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Root,
        ),
        "lmu_windows" to listOf(
            ReadoutItemKey.LmuWindows.Flag.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
        ),
        "rFactor 2" to listOf(ReadoutItemKey.LmuWindows.Flag.Root),
    ).forEach { (simulator, newOrder) ->
        coEvery { repository.saveReadoutOrder(simulator, newOrder) } answers {
            order.update { all -> all + (simulator to newOrder) }
        }
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
        io.mockk.verify(exactly = 2) { repo.observeReadoutOrder("lmu_windows") }
        io.mockk.verify(exactly = 1) { repo.observeReadoutOrder("rFactor 2") }
        io.mockk.coVerify(exactly = 1) {
            repo.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    ReadoutItemKey.LmuWindows.Flag.Root,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                ),
            )
        }
        io.mockk.coVerify(exactly = 1) {
            repo.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.LmuWindows.Flag.Root))
        }
        io.mockk.confirmVerified(repo)
    }
}
