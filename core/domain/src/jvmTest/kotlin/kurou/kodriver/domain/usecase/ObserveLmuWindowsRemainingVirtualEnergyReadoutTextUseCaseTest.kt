package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCaseTest {
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository = mockk()

    @Test
    fun `バーチャルエナジー読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("残量{percent}%")
            val useCase = ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(repository)

            assertEquals("残量{percent}%", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
