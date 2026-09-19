package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsRemainingFuelThresholdPercentageUseCaseTest {
    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    @Test
    fun `リポジトリの燃料残量閾値を返す`() =
        runTest {
            val threshold = MutableStateFlow(30)
            every { repository.observeThresholdPercentage() } returns threshold
            val useCase = ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(repository)

            assertEquals(30, useCase().first())
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            confirmVerified(repository)
        }
}
