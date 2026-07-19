package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5RemainingFuelLapsUseCaseTest {

    @MockK
    private lateinit var repository: Gt7Ps5RemainingFuelLapsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `燃料残り周回数を監視できる`() = runBlocking {
        every { repository.observeRemainingFuelLaps() } returns MutableStateFlow(5)
        val useCase = ObserveGt7Ps5RemainingFuelLapsUseCase(repository)

        assertEquals(5, useCase().first())
        verify(exactly = 1) { repository.observeRemainingFuelLaps() }
        confirmVerified(repository)
    }
}
