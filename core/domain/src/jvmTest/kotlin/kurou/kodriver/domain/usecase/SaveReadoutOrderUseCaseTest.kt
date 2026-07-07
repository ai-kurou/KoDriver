package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutOrderUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)
        val observeUseCase = ObserveReadoutOrderUseCase(repo)

        saveUseCase(
            "lmu_windows",
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.Flag,
                ReadoutItemKey.LmuWindows.VehicleDamage,
            ),
        )
        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.VehicleApproach,
                ReadoutItemKey.LmuWindows.Flag,
                ReadoutItemKey.LmuWindows.VehicleDamage,
            ),
            observeUseCase("lmu_windows").first(),
        )

        saveUseCase(
            "lmu_windows",
            listOf(
                ReadoutItemKey.LmuWindows.Flag,
                ReadoutItemKey.LmuWindows.VehicleDamage,
                ReadoutItemKey.LmuWindows.VehicleApproach,
            ),
        )
        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag,
                ReadoutItemKey.LmuWindows.VehicleDamage,
                ReadoutItemKey.LmuWindows.VehicleApproach,
            ),
            observeUseCase("lmu_windows").first(),
        )
    }
}
