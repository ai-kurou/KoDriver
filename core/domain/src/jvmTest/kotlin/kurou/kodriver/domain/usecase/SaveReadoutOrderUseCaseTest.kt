package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutOrderUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)
        val observeUseCase = ObserveReadoutOrderUseCase(repo)

        saveUseCase("lmu", listOf("vehicle_approach", "laps_remaining"))
        assertEquals(listOf("vehicle_approach", "laps_remaining"), observeUseCase("lmu").first())

        saveUseCase("lmu", listOf("laps_remaining", "vehicle_approach"))
        assertEquals(listOf("laps_remaining", "vehicle_approach"), observeUseCase("lmu").first())
    }
}
