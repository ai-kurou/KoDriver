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

class ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    @Test
    fun `バーチャルエナジー予想残り周回数を監視できる`() =
        runTest {
            every { repository.observeVirtualEnergyLaps() } returns MutableStateFlow(5)
            val useCase = ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository)

            assertEquals(5, useCase().first())
            verify(exactly = 1) { repository.observeVirtualEnergyLaps() }
            confirmVerified(repository)
        }
}
