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

class ObserveAceWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk()

    @Test
    fun `燃料残量読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("残り{percent}パーセント")
            val useCase = ObserveAceWindowsRemainingFuelReadoutTextUseCase(repository)

            assertEquals("残り{percent}パーセント", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
