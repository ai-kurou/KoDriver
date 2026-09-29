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

class ObserveLmuWindowsBlackFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeBlackFlagText() } returns flowOf("ブラック、停止")

            assertEquals("ブラック、停止", ObserveLmuWindowsBlackFlagReadoutTextUseCase(repository)().first())
            verify(exactly = 1) { repository.observeBlackFlagText() }
            confirmVerified(repository)
        }
}
