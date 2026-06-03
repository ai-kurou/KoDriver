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

        assertTrue(useCase("Le Mans Ultimate").first().isEmpty())

        repo.saveReadoutEnabledState("Le Mans Ultimate", "車両接近", true)
        repo.saveReadoutEnabledState("rFactor 2", "車両接近", false)

        assertEquals(mapOf("車両接近" to true), useCase("Le Mans Ultimate").first())
        assertEquals(mapOf("車両接近" to false), useCase("rFactor 2").first())
    }
}
