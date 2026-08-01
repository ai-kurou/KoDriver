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
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveSelectedSimulatorUseCaseTest {

    @MockK
    private lateinit var repo: SimulatorPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値がnullのときnullを返し・保存済みの値をそのまま返す`() =
        runBlocking {
        val state = MutableStateFlow<Simulator?>(null)
        every { repo.selectedSimulator() } returns state
        listOf(Simulator.LmuWindows).forEach { simulator ->
            coEvery { repo.saveSelectedSimulator(simulator) } answers { state.update { simulator } }
        }
        val useCase = ObserveSelectedSimulatorUseCase(repo)

        assertNull(useCase().first())

        repo.saveSelectedSimulator(Simulator.LmuWindows)
        assertEquals(Simulator.LmuWindows, useCase().first())

        verify(exactly = 2) { repo.selectedSimulator() }
        coVerify(exactly = 1) { repo.saveSelectedSimulator(Simulator.LmuWindows) }
        confirmVerified(repo)
    }
}
