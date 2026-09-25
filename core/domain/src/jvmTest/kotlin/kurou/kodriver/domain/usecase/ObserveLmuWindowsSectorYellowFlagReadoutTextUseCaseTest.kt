package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsSectorYellowFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeSectorYellowFlagText() } returns flowOf("イエロー、注意")

            assertEquals(
                "イエロー、注意",
                ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(repository)().first(),
            )
        }
}
