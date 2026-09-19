package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5RemainingFuelThresholdPercentageUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository = mockk()

    @Test
    fun `リポジトリの燃料残量閾値を返す`() =
        runTest {
            val threshold = MutableStateFlow(30)
            every { repository.observeThresholdPercentage() } returns threshold
            val useCase = ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository)

            assertEquals(30, useCase().first())
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            confirmVerified(repository)
        }
}
