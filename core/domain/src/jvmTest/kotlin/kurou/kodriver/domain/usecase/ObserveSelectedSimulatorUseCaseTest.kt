package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSelectedSimulatorUseCaseTest {
    @MockK
    private lateinit var repo: SimulatorPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はLmuWindowsを返し・保存済みの値をそのまま返す`() =
        runTest {
            val state = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            every { repo.selectedSimulator() } returns state
            coEvery { repo.saveSelectedSimulator(Simulator.Gt7Ps5) } answers { state.update { Simulator.Gt7Ps5 } }
            val useCase = ObserveSelectedSimulatorUseCase(repo)

            assertEquals(Simulator.LmuWindows, useCase().first())

            repo.saveSelectedSimulator(Simulator.Gt7Ps5)
            assertEquals(Simulator.Gt7Ps5, useCase().first())

            verify(exactly = 2) { repo.selectedSimulator() }
            coVerify(exactly = 1) { repo.saveSelectedSimulator(Simulator.Gt7Ps5) }
            confirmVerified(repo)
        }
}
