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

class ObserveLmuWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk()

    @Test
    fun `燃料読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns flowOf("残量{percent}%")
            assertEquals("残量{percent}%", ObserveLmuWindowsRemainingFuelReadoutTextUseCase(repository)().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
