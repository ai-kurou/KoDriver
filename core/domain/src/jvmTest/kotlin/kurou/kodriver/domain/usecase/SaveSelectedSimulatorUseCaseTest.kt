package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveSelectedSimulatorUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映される`() = runBlocking {
        val repo = FakeSimulatorPreferencesRepository()
        val saveUseCase = SaveSelectedSimulatorUseCase(repo)
        val observeUseCase = ObserveSelectedSimulatorUseCase(repo)

        saveUseCase(Simulator.LmuWindows)

        assertEquals(Simulator.LmuWindows, observeUseCase().first())
    }
}
