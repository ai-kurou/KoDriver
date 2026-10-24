package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRemainingFuelThresholdPercentageUseCaseTest {
    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値を返す`() =
        runTest {
            every { repository.observeThresholdPercentage() } returns flowOf(20)
            assertEquals(20, ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(repository)().first())
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            confirmVerified(repository)
        }
}
