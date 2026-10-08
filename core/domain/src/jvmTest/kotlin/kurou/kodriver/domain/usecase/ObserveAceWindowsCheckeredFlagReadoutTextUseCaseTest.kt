package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsCheckeredFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) } returns flowOf("チェッカー、完走")

            assertEquals(
                "チェッカー、完走",
                ObserveAceWindowsCheckeredFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED) }
            confirmVerified(repository)
        }
}
