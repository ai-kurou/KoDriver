package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5RemainingFuelLapsUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk()

    @Test
    fun `燃料残り周回数を監視できる`() =
        runTest {
            every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(5)
            val useCase = ObserveGt7Ps5RemainingFuelLapsUseCase(repository)

            assertEquals(5, useCase().first())
            verify(exactly = 1) { repository.observeRemainingFuelLaps() }
            confirmVerified(repository)
        }
}
