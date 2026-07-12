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

class SaveReadoutEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = createReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutEnabledStateUseCase(repo)
        val observeUseCase = ObserveReadoutEnabledStatesUseCase(repo)

        saveUseCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
            ),
            observeUseCase("lmu_windows").first(),
        )

        saveUseCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
            ),
            observeUseCase("lmu_windows").first(),
        )
    }
}
