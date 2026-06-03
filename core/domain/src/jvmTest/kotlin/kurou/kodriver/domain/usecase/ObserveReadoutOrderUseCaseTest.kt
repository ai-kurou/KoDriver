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

        repo.saveReadoutOrder("lmu", listOf("vehicle_approach", "laps_remaining"))
        repo.saveReadoutOrder("rFactor 2", listOf("laps_remaining"))

        assertEquals(listOf("vehicle_approach", "laps_remaining"), useCase("lmu").first())
        assertEquals(listOf("laps_remaining"), useCase("rFactor 2").first())
    }
}
