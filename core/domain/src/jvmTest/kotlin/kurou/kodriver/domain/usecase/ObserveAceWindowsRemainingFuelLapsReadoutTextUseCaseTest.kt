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

class ObserveAceWindowsRemainingFuelLapsReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk()

    @Test
    fun `燃料残り周回数読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("残り{laps}周")
            val useCase = ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase(repository)

            assertEquals("残り{laps}周", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
