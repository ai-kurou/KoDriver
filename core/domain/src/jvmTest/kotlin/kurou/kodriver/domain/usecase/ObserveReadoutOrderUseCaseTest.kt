package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    @Test
    fun `空リストで上書きすると空リストを返す`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        repo.saveReadoutOrder("lmu", listOf("vehicle_approach", "laps_remaining"))
        repo.saveReadoutOrder("lmu", emptyList())

        assertTrue(useCase("lmu").first().isEmpty())
    }

    @Test
    fun `Flowは順序が更新されるたびに新しい値を発行する`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        repo.saveReadoutOrder("lmu", listOf("vehicle_approach", "laps_remaining"))

        val updatedJob = launch {
            val updated = useCase("lmu").drop(1).first()
            assertEquals(listOf("laps_remaining", "vehicle_approach"), updated)
        }

        repo.saveReadoutOrder("lmu", listOf("laps_remaining", "vehicle_approach"))
        updatedJob.join()
    }

    @Test
    fun `空文字列のシミュレーター名でも初期値は空リスト`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        assertTrue(useCase("").first().isEmpty())
    }

    @Test
    fun `1件のみの順序リストを正しく返す`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val useCase = ObserveReadoutOrderUseCase(repo)

        repo.saveReadoutOrder("lmu", listOf("vehicle_approach"))

        assertEquals(listOf("vehicle_approach"), useCase("lmu").first())
    }
}
