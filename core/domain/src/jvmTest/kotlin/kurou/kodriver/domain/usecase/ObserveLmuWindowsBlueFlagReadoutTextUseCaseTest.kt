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

class ObserveLmuWindowsBlueFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeBlueFlagText() } returns flowOf("ブルー、道を譲れ")

            assertEquals(
                "ブルー、道を譲れ",
                ObserveLmuWindowsBlueFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeBlueFlagText() }
            confirmVerified(repository)
        }
}
