package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsVehicleApproachReadoutTextUseCaseTest {
    private val repository: AceWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeReadoutText() } returns flowOf("車両接近")

            assertEquals(
                "車両接近",
                ObserveAceWindowsVehicleApproachReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
