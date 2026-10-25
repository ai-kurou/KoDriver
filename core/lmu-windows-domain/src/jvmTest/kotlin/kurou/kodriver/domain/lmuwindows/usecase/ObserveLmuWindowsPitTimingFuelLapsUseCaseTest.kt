package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingFuelLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    @Test
    fun `燃料予想残り周回数を監視できる`() =
        runTest {
            every { repository.observeFuelLaps() } returns MutableStateFlow(2)
            val useCase = ObserveLmuWindowsPitTimingFuelLapsUseCase(repository)

            assertEquals(2, useCase().first())
            verify(exactly = 1) { repository.observeFuelLaps() }
            confirmVerified(repository)
        }
}
