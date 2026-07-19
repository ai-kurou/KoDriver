package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
        val state = MutableStateFlow(90)
        every { repo.observeHighThresholdCelsius() } returns state
        coEvery { repo.saveHighThresholdCelsius(110) } answers { state.update { 110 } }
        val useCase = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

        assertEquals(90, useCase().first())

        repo.saveHighThresholdCelsius(110)
        assertEquals(110, useCase().first())

        io.mockk.verify(exactly = 2) { repo.observeHighThresholdCelsius() }
        io.mockk.coVerify(exactly = 1) { repo.saveHighThresholdCelsius(110) }
        io.mockk.confirmVerified(repo)
    }
}
