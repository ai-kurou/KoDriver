package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveReadoutEnabledStatesUseCaseTest {

    @Test
    fun `初期値は空Map・保存済みの値を返す・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertTrue(useCase("lmu_windows").first().isEmpty())

        repo.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        repo.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.VEHICLE_APPROACH, false)

        assertEquals(mapOf(ReadoutItemKey.VEHICLE_APPROACH to true), useCase("lmu_windows").first())
        assertEquals(mapOf(ReadoutItemKey.VEHICLE_APPROACH to false), useCase("rFactor 2").first())
    }
}
