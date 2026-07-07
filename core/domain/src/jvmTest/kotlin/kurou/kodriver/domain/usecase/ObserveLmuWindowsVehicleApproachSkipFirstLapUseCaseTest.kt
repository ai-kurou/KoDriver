package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `保存されたskipFirstLapがtrueのときtrueを返す`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = true)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertTrue(useCase().first())
    }

    @Test
    fun `保存されたskipFirstLapがfalseのときfalseを返す`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertFalse(useCase().first())
    }
}
