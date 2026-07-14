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
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(): LmuWindowsVehicleApproachPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    coEvery { repository.saveEnabledState(any(), any()) } answers {
        enabledStates.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class ObserveLmuWindowsVehicleApproachEnabledStatesUseCaseTest {

    @Test
    fun `初期値はSustainedとStartReadoutのデフォルトtrueを返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachPreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true,
            ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to true,
        )
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachPreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true,
            ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false,
        )
        assertEquals(expected, useCase().first())
    }
}
