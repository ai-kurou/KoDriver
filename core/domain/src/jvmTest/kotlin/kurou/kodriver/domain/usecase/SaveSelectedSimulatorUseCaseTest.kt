package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test

class SaveSelectedSimulatorUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映される`() = runBlocking {
        val repository = mockk<SimulatorPreferencesRepository>(relaxUnitFun = true)

        SaveSelectedSimulatorUseCase(repository)(Simulator.LmuWindows)

        coVerify(exactly = 1) { repository.saveSelectedSimulator(Simulator.LmuWindows) }
        confirmVerified(repository)
    }
}
