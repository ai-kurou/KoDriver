package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveReadoutEnabledStatesUseCaseTest {

    @Test
    fun `初期値は空Map・保存済みの値を返す・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutEnabledStatesUseCase(repo)

        assertTrue(useCase("lmu_windows").first().isEmpty())

        repo.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        repo.saveReadoutEnabledState("rFactor 2", "vehicle_approach", false)

        assertEquals(mapOf("vehicle_approach" to true), useCase("lmu_windows").first())
        assertEquals(mapOf("vehicle_approach" to false), useCase("rFactor 2").first())
    }
}
