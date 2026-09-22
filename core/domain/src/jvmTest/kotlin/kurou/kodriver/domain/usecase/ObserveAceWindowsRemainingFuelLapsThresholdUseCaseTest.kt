package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsRemainingFuelLapsThresholdUseCaseTest {
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    @Test
    fun `燃料残り周回数を監視できる`() =
        runTest {
            every { repository.observeThresholdLaps() } returns MutableStateFlow(5)
            val useCase = ObserveAceWindowsRemainingFuelLapsThresholdUseCase(repository)

            assertEquals(5, useCase().first())
            verify(exactly = 1) { repository.observeThresholdLaps() }
            confirmVerified(repository)
        }
}
