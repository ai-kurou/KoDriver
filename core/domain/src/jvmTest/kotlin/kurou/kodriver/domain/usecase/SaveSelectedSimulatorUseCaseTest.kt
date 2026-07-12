package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createSimulatorPreferencesRepository(initial: Simulator? = null): SimulatorPreferencesRepository {
    val repository = mockk<SimulatorPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.selectedSimulator() } returns state
    coEvery { repository.saveSelectedSimulator(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveSelectedSimulatorUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映される`() = runBlocking {
        val repo = createSimulatorPreferencesRepository()
        val saveUseCase = SaveSelectedSimulatorUseCase(repo)
        val observeUseCase = ObserveSelectedSimulatorUseCase(repo)

        saveUseCase(Simulator.LmuWindows)

        assertEquals(Simulator.LmuWindows, observeUseCase().first())
    }
}
