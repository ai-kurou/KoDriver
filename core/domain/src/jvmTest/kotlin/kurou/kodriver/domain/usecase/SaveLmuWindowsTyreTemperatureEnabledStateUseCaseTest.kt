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
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(): LmuWindowsTyreTemperaturePreferencesRepository {
    val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    coEvery { repository.saveEnabledState(any(), any()) } answers {
        enabledStates.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class SaveLmuWindowsTyreTemperatureEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
        val saveUseCase = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repo)
        val observeUseCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        saveUseCase(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
            ),
            observeUseCase().first(),
        )

        saveUseCase(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
            ),
            observeUseCase().first(),
        )
    }
}
