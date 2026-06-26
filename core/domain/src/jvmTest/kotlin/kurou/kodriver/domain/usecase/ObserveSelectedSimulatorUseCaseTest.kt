package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveSelectedSimulatorUseCaseTest {

    @Test
    fun `初期値がnullのときnullを返し・保存済みの値をそのまま返す`() = runBlocking {
        val repo = FakeSimulatorPreferencesRepository(initial = null)
        val useCase = ObserveSelectedSimulatorUseCase(repo)

        assertNull(useCase().first())

        repo.saveSelectedSimulator(Simulator.LmuWindows)
        assertEquals(Simulator.LmuWindows, useCase().first())
    }
}
