package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsYellowFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW) } returns flowOf("イエローフラッグ")

            assertEquals(
                "イエローフラッグ",
                ObserveAceWindowsYellowFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW) }
            confirmVerified(repository)
        }
}
