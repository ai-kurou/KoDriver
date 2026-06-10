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

        saveUseCase("lmu", listOf("vehicle_approach", "flag"))
        assertEquals(listOf("vehicle_approach", "flag"), observeUseCase("lmu").first())

        saveUseCase("lmu", listOf("flag", "vehicle_approach"))
        assertEquals(listOf("flag", "vehicle_approach"), observeUseCase("lmu").first())
    }
}
