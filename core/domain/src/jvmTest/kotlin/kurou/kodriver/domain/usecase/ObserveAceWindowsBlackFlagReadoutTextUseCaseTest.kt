package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsBlackFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeBlackFlagText() } returns flowOf("ブラックフラッグ")

            assertEquals(
                "ブラックフラッグ",
                ObserveAceWindowsBlackFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeBlackFlagText() }
            confirmVerified(repository)
        }
}
