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

class ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk()

    @Test
    fun `燃料残り周回数読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("残り{laps}周")
            val useCase = ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase(repository)

            assertEquals("残り{laps}周", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
