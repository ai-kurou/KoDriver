package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun createReadoutPreferencesRepository(repository: ReadoutPreferencesRepository): ReadoutPreferencesRepository {
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
        "lmu_windows" to
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ),
        "lmu_windows" to
            listOf(
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
    @MockK
    private lateinit var repository: ReadoutPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値は空リスト・保存済みの順序を返す・シミュレーターごとに独立している`() =
        runTest {
            val repo = createReadoutPreferencesRepository(repository)
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
            verify(exactly = 2) { repo.observeReadoutOrder("lmu_windows") }
            verify(exactly = 1) { repo.observeReadoutOrder("rFactor 2") }
            coVerify(exactly = 1) {
                repo.saveReadoutOrder(
                    "lmu_windows",
                    listOf(
                        ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                        ReadoutItemKey.LmuWindows.Flag.Root,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    ),
                )
            }
            coVerify(exactly = 1) {
                repo.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.LmuWindows.Flag.Root))
            }
            confirmVerified(repo)
        }
}
