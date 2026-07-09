@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsTyreTemperatureEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
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
