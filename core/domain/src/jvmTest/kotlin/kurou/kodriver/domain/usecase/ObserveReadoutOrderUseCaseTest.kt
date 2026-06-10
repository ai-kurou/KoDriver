package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveReadoutOrderUseCaseTest {

    @Test
    fun `初期値は空リスト・保存済みの順序を返す・シミュレーターごとに独立している`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        assertTrue(useCase("lmu").first().isEmpty())

        repo.saveReadoutOrder("lmu", listOf("vehicle_approach", "flag"))
        repo.saveReadoutOrder("rFactor 2", listOf("flag"))

        assertEquals(listOf("vehicle_approach", "flag"), useCase("lmu").first())
        assertEquals(listOf("flag"), useCase("rFactor 2").first())
    }
}
