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

class ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeFullCourseYellowFlagText() } returns flowOf("フルコースイエロー")

            assertEquals(
                "フルコースイエロー",
                ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeFullCourseYellowFlagText() }
            confirmVerified(repository)
        }
}
