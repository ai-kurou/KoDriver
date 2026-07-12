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
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleDamagePreferencesRepository(): LmuWindowsVehicleDamagePreferencesRepository {
    val repository = mockk<LmuWindowsVehicleDamagePreferencesRepository>()
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns states
    coEvery { repository.saveEnabledState(any(), any()) } answers {
        states.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class SaveLmuWindowsVehicleDamageEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = createLmuWindowsVehicleDamagePreferencesRepository()
        val saveUseCase = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repo)
        val observeUseCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        saveUseCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true),
            observeUseCase().first(),
        )

        saveUseCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false),
            observeUseCase().first(),
        )
    }
}
