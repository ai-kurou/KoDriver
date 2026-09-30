package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRedFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeRedFlagText() } returns flowOf("レッドフラッグ、セッション中断")

            assertEquals(
                "レッドフラッグ、セッション中断",
                ObserveLmuWindowsRedFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeRedFlagText() }
            confirmVerified(repository)
        }
}
