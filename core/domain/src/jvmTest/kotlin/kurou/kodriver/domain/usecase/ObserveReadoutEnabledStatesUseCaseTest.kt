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

class ObserveReadoutEnabledStatesUseCaseTest {

    @Test
    fun `初期値はデフォルト定義のない未知のシミュレーターでは空Mapを返す`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertTrue(useCase("rFactor 2").first().isEmpty())
        io.mockk.verify(exactly = 1) { repo.observeReadoutEnabledStates("rFactor 2") }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `lmu_windowsは保存済みの値がなくてもデフォルト値が反映される`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root to true,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
            ),
            useCase("lmu_windows").first(),
        )
        io.mockk.verify(exactly = 1) { repo.observeReadoutEnabledStates("lmu_windows") }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `gt7_ps5は保存済みの値がなくてもデフォルトのtrueが反映される`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
            ),
            useCase("gt7_ps5").first(),
        )
        io.mockk.verify(exactly = 1) { repo.observeReadoutEnabledStates("gt7_ps5") }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `保存済みの値はデフォルトより優先され・シミュレーターごとに独立している`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
        repo.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.LmuWindows.VehicleApproach.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root to true,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
            ),
            useCase("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            useCase("rFactor 2").first(),
        )
        io.mockk.coVerify(exactly = 1) {
            repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
        }
        io.mockk.coVerify(exactly = 1) {
            repo.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.LmuWindows.VehicleApproach.Root, false)
        }
        io.mockk.verify(exactly = 1) { repo.observeReadoutEnabledStates("lmu_windows") }
        io.mockk.verify(exactly = 1) { repo.observeReadoutEnabledStates("rFactor 2") }
        io.mockk.confirmVerified(repo)
    }
}
