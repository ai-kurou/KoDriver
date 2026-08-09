package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveSelectedSimulatorUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: SimulatorPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映される`() =
        runTest {
            SaveSelectedSimulatorUseCase(repository)(Simulator.LmuWindows)

            coVerify(exactly = 1) { repository.saveSelectedSimulator(Simulator.LmuWindows) }
            confirmVerified(repository)
        }
}
