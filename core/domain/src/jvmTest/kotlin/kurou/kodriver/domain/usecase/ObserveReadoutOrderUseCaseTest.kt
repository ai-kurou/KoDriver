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

        assertTrue(useCase("lmu_windows").first().isEmpty())

        repo.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag", "vehicle_damage"))
        repo.saveReadoutOrder("rFactor 2", listOf("flag"))

        assertEquals(listOf("vehicle_approach", "flag", "vehicle_damage"), useCase("lmu_windows").first())
        assertEquals(listOf("flag"), useCase("rFactor 2").first())
    }
}
