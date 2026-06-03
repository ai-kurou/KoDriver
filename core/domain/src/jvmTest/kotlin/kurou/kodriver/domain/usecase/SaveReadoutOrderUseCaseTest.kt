package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `空リストを保存すると順序が空になる`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)
        val observeUseCase = ObserveReadoutOrderUseCase(repo)

        saveUseCase("lmu", listOf("vehicle_approach", "laps_remaining"))
        saveUseCase("lmu", emptyList())

        assertTrue(observeUseCase("lmu").first().isEmpty())
    }

    @Test
    fun `一方のシミュレータへの保存が他方に影響しない`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)
        val observeUseCase = ObserveReadoutOrderUseCase(repo)

        saveUseCase("lmu", listOf("vehicle_approach", "laps_remaining"))
        saveUseCase("rFactor 2", listOf("laps_remaining"))

        assertEquals(listOf("vehicle_approach", "laps_remaining"), observeUseCase("lmu").first())
        assertEquals(listOf("laps_remaining"), observeUseCase("rFactor 2").first())
    }

    @Test
    fun `順序の保存がenabledStatesに影響しない`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)

        repo.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        saveUseCase("lmu", listOf("vehicle_approach", "laps_remaining"))

        assertEquals(mapOf("vehicle_approach" to true), repo.observeReadoutEnabledStates("lmu").first())
    }

    @Test
    fun `1件のみのリストを保存できる`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutOrderUseCase(repo)
        val observeUseCase = ObserveReadoutOrderUseCase(repo)

        saveUseCase("lmu", listOf("vehicle_approach"))

        assertEquals(listOf("vehicle_approach"), observeUseCase("lmu").first())
    }
}
