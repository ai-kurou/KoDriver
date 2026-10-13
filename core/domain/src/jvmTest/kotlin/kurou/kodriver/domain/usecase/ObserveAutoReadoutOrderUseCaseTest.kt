package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun createAutoReadoutPreferencesRepository(
    repository: AutoReadoutPreferencesRepository,
): AutoReadoutPreferencesRepository {
    val enabledStates = MutableStateFlow<Map<String, Map<ReadoutItemKey, Boolean>>>(emptyMap())
    val order = MutableStateFlow<Map<String, List<ReadoutItemKey>>>(emptyMap())
    listOf("lmu_windows", "gt7_ps5", "rFactor 2").forEach { simulator ->
        every { repository.observeAutoReadoutEnabledStates(simulator) } answers {
            enabledStates.map { it[simulator] ?: emptyMap() }
        }
        every { repository.observeAutoReadoutOrder(simulator) } answers {
            order.map { it[simulator] ?: emptyList() }
        }
    }
    listOf(
        Triple("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, true),
        Triple("lmu_windows", LmuWindowsReadoutItemKey.MyBestLap.Root, false),
        Triple("rFactor 2", LmuWindowsReadoutItemKey.VehicleApproach.Root, false),
    ).forEach { (simulator, key, enabled) ->
        coEvery { repository.saveAutoReadoutEnabledState(simulator, key, enabled) } answers {
            enabledStates.update { all -> all + (simulator to ((all[simulator] ?: emptyMap()) + (key to enabled))) }
        }
    }
    listOf(
        "lmu_windows" to
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
            ),
        "lmu_windows" to
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
            ),
        "rFactor 2" to listOf(LmuWindowsReadoutItemKey.Flag.Root),
    ).forEach { (simulator, newOrder) ->
        coEvery { repository.saveAutoReadoutOrder(simulator, newOrder) } answers {
            order.update { all -> all + (simulator to newOrder) }
        }
    }
    return repository
}

class ObserveAutoReadoutOrderUseCaseTest {
    private val repository: AutoReadoutPreferencesRepository = mockk()

    @Test
    fun `初期値は空リスト・保存済みの順序を返す・シミュレーターごとに独立している`() =
        runTest {
            val repo = createAutoReadoutPreferencesRepository(repository)
            val useCase = ObserveAutoReadoutOrderUseCase(repo)

            assertTrue(useCase("lmu_windows").first().isEmpty())

            repo.saveAutoReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
            )
            repo.saveAutoReadoutOrder("rFactor 2", listOf(LmuWindowsReadoutItemKey.Flag.Root))

            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                useCase("lmu_windows").first(),
            )
            assertEquals(listOf(LmuWindowsReadoutItemKey.Flag.Root), useCase("rFactor 2").first())
            verify(exactly = 2) { repo.observeAutoReadoutOrder("lmu_windows") }
            verify(exactly = 1) { repo.observeAutoReadoutOrder("rFactor 2") }
            coVerify(exactly = 1) {
                repo.saveAutoReadoutOrder(
                    "lmu_windows",
                    listOf(
                        LmuWindowsReadoutItemKey.VehicleApproach.Root,
                        LmuWindowsReadoutItemKey.Flag.Root,
                        LmuWindowsReadoutItemKey.VehicleDamage.Root,
                    ),
                )
            }
            coVerify(exactly = 1) {
                repo.saveAutoReadoutOrder("rFactor 2", listOf(LmuWindowsReadoutItemKey.Flag.Root))
            }
            confirmVerified(repo)
        }
}
