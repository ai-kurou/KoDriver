package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `trueを渡すとskipFirstLapがtrueとして保存される`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCase = SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        useCase(true)

        assertTrue(repository.observeSkipFirstLap().first())
    }

    @Test
    fun `falseを渡すとskipFirstLapがfalseとして保存される`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = true)
        val useCase = SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        useCase(false)

        assertFalse(repository.observeSkipFirstLap().first())
    }
}
