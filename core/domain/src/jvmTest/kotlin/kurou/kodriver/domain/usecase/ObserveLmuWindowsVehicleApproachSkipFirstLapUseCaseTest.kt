package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `保存されたskipFirstLapがtrueのときtrueを返す`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
        every { repository.observeSkipFirstLap() } returns MutableStateFlow(true)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertTrue(useCase().first())
    }

    @Test
    fun `保存されたskipFirstLapがfalseのときfalseを返す`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
        every { repository.observeSkipFirstLap() } returns MutableStateFlow(false)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertFalse(useCase().first())
    }
}
