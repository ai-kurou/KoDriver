package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test

class SaveSelectedSimulatorUseCaseTest {
    private val repository: SimulatorPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映される`() =
        runTest {
            SaveSelectedSimulatorUseCase(repository)(Simulator.LmuWindows)

            coVerify(exactly = 1) { repository.saveSelectedSimulator(Simulator.LmuWindows) }
            confirmVerified(repository)
        }
}
