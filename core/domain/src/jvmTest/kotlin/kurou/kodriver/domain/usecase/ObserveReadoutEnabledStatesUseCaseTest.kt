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

class ObserveReadoutEnabledStatesUseCaseTest {

    @Test
    fun `初期値はデフォルト定義のない未知のシミュレーターでは空Mapを返す`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertTrue(useCase("rFactor 2").first().isEmpty())
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
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
            ),
            useCase("lmu_windows").first(),
        )
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
                ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
            ),
            useCase("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Root to false),
            useCase("rFactor 2").first(),
        )
    }
}
