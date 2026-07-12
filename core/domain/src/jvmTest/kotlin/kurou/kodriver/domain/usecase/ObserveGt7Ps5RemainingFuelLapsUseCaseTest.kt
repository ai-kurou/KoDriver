package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5RemainingFuelLapsUseCaseTest {

    @Test
    fun `燃料残り周回数を監視できる`() = runBlocking {
        val repository = mockk<Gt7Ps5RemainingFuelLapsPreferencesRepository>()
        every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(5)
        val useCase = ObserveGt7Ps5RemainingFuelLapsUseCase(repository)

        assertEquals(5, useCase().first())
    }
}
