package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `trueを渡すとskipFirstLapがtrueとして保存される`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCase = SaveVehicleApproachSkipFirstLapUseCase(repository)

        useCase(true)

        assertTrue(repository.observeSkipFirstLap().first())
    }

    @Test
    fun `falseを渡すとskipFirstLapがfalseとして保存される`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(initialSkipFirstLap = true)
        val useCase = SaveVehicleApproachSkipFirstLapUseCase(repository)

        useCase(false)

        assertFalse(repository.observeSkipFirstLap().first())
    }
}
