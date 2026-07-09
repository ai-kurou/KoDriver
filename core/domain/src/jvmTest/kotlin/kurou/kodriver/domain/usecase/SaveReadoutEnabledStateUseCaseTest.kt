package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutEnabledStateUseCase(repo)
        val observeUseCase = ObserveReadoutEnabledStatesUseCase(repo)

        saveUseCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
            ),
            observeUseCase("lmu_windows").first(),
        )

        saveUseCase("lmu_windows", ReadoutItemKey.LmuWindows.MyBestLap.Root, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
            ),
            observeUseCase("lmu_windows").first(),
        )
    }
}
