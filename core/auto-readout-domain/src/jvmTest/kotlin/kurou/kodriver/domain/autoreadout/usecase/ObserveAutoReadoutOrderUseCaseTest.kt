package kurou.kodriver.domain.autoreadout.usecase

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
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey
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
        Triple("lmu_windows", TestAutoReadoutItemKey.MyBestLap, true),
        Triple("lmu_windows", TestAutoReadoutItemKey.MyBestLap, false),
        Triple("rFactor 2", TestAutoReadoutItemKey.VehicleApproach, false),
    ).forEach { (simulator, key, enabled) ->
        coEvery { repository.saveAutoReadoutEnabledState(simulator, key, enabled) } answers {
            enabledStates.update { all -> all + (simulator to ((all[simulator] ?: emptyMap()) + (key to enabled))) }
        }
    }
    listOf(
        "lmu_windows" to
            listOf(
                TestAutoReadoutItemKey.VehicleApproach,
                TestAutoReadoutItemKey.Flag,
                TestAutoReadoutItemKey.VehicleDamage,
            ),
        "lmu_windows" to
            listOf(
                TestAutoReadoutItemKey.Flag,
                TestAutoReadoutItemKey.VehicleDamage,
                TestAutoReadoutItemKey.VehicleApproach,
            ),
        "rFactor 2" to listOf(TestAutoReadoutItemKey.Flag),
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
                    TestAutoReadoutItemKey.VehicleApproach,
                    TestAutoReadoutItemKey.Flag,
                    TestAutoReadoutItemKey.VehicleDamage,
                ),
            )
            repo.saveAutoReadoutOrder("rFactor 2", listOf(TestAutoReadoutItemKey.Flag))

            assertEquals(
                listOf(
                    TestAutoReadoutItemKey.VehicleApproach,
                    TestAutoReadoutItemKey.Flag,
                    TestAutoReadoutItemKey.VehicleDamage,
                ),
                useCase("lmu_windows").first(),
            )
            assertEquals(listOf(TestAutoReadoutItemKey.Flag), useCase("rFactor 2").first())
            verify(exactly = 2) { repo.observeAutoReadoutOrder("lmu_windows") }
            verify(exactly = 1) { repo.observeAutoReadoutOrder("rFactor 2") }
            coVerify(exactly = 1) {
                repo.saveAutoReadoutOrder(
                    "lmu_windows",
                    listOf(
                        TestAutoReadoutItemKey.VehicleApproach,
                        TestAutoReadoutItemKey.Flag,
                        TestAutoReadoutItemKey.VehicleDamage,
                    ),
                )
            }
            coVerify(exactly = 1) {
                repo.saveAutoReadoutOrder("rFactor 2", listOf(TestAutoReadoutItemKey.Flag))
            }
            confirmVerified(repo)
        }
}
