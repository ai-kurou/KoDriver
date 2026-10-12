package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `保存されたskipFirstLapがtrueのときtrueを返す`() =
        runTest {
            every { repository.observeSkipFirstLap() } returns MutableStateFlow(true)
            val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

            assertTrue(useCase().first())
            verify(exactly = 1) { repository.observeSkipFirstLap() }
            confirmVerified(repository)
        }

    @Test
    fun `保存されたskipFirstLapがfalseのときfalseを返す`() =
        runTest {
            every { repository.observeSkipFirstLap() } returns MutableStateFlow(false)
            val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

            assertFalse(useCase().first())
            verify(exactly = 1) { repository.observeSkipFirstLap() }
            confirmVerified(repository)
        }
}
