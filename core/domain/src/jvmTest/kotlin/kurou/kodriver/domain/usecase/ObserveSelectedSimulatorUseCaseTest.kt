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
import kotlin.test.assertNull

class ObserveSelectedSimulatorUseCaseTest {

    @Test
    fun `初期値がnullのときnullを返し・保存済みの値をそのまま返す`() = runBlocking {
        val repo = mockk<SimulatorPreferencesRepository>()
        val state = MutableStateFlow<Simulator?>(null)
        every { repo.selectedSimulator() } returns state
        coEvery { repo.saveSelectedSimulator(any()) } answers { state.update { firstArg() } }
        val useCase = ObserveSelectedSimulatorUseCase(repo)

        assertNull(useCase().first())

        repo.saveSelectedSimulator(Simulator.LmuWindows)
        assertEquals(Simulator.LmuWindows, useCase().first())
    }
}
