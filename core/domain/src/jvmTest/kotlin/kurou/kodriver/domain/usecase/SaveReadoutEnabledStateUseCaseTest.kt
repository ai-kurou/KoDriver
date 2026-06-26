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

        saveUseCase("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to true),
            observeUseCase("lmu_windows").first(),
        )

        saveUseCase("lmu_windows", ReadoutItemKey.VehicleApproach, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to false),
            observeUseCase("lmu_windows").first(),
        )
    }
}
