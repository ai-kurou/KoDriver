package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsRemainingFuelThresholdPercentageUseCaseTest {
    @MockK
    private lateinit var repository: AceWindowsRemainingFuelPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `リポジトリの燃料残量閾値を返す`() =
        runBlocking {
            val threshold = MutableStateFlow(30)
            every { repository.observeThresholdPercentage() } returns threshold
            val useCase = ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(repository)

            assertEquals(30, useCase().first())
            verify(exactly = 1) { repository.observeThresholdPercentage() }
            confirmVerified(repository)
        }
}
