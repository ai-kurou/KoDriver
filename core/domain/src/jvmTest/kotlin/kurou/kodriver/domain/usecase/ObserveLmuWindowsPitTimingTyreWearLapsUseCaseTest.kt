package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingTyreWearLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    @Test
    fun `タイヤ摩耗予想残り周回数を監視できる`() =
        runTest {
            every { repository.observeTyreWearLaps() } returns MutableStateFlow(2)
            val useCase = ObserveLmuWindowsPitTimingTyreWearLapsUseCase(repository)

            assertEquals(2, useCase().first())
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            confirmVerified(repository)
        }
}
