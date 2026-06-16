package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutEnabledStateUseCase(repo)
        val observeUseCase = ObserveReadoutEnabledStatesUseCase(repo)

        saveUseCase("lmu_windows", "vehicle_approach", true)
        assertEquals(mapOf("vehicle_approach" to true), observeUseCase("lmu_windows").first())

        saveUseCase("lmu_windows", "vehicle_approach", false)
        assertEquals(mapOf("vehicle_approach" to false), observeUseCase("lmu_windows").first())
    }
}
