@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createQueuePreferencesRepository(): QueuePreferencesRepository {
    val repository = mockk<QueuePreferencesRepository>()
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeQueueEnabledStates() } returns states
    coEvery { repository.saveQueueEnabledState(any(), any()) } answers {
        states.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class ObserveQueueEnabledStatesUseCaseTest {

    @Test
    fun `初期値はsupportsQueue対象項目のデフォルトfalseを返す`() = runBlocking {
        val repo = createQueuePreferencesRepository()
        val useCase = ObserveQueueEnabledStatesUseCase(repo)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to false,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false,
            ),
            useCase().first(),
        )
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = createQueuePreferencesRepository()
        val useCase = ObserveQueueEnabledStatesUseCase(repo)

        repo.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false,
            ),
            useCase().first(),
        )
    }
}
