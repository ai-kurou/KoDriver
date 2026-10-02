package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeSustainedLeftReadoutText() } returns flowOf("左注意")

            assertEquals(
                "左注意",
                ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeSustainedLeftReadoutText() }
            confirmVerified(repository)
        }
}
