package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    initialSkipFirstLap: Boolean = true,
): LmuWindowsVehicleApproachPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
    val state = MutableStateFlow(initialSkipFirstLap)
    every { repository.observeSkipFirstLap() } returns state
    coEvery { repository.saveSkipFirstLap(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `trueを渡すとskipFirstLapがtrueとして保存される`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCase = SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        useCase(true)

        assertTrue(repository.observeSkipFirstLap().first())
    }

    @Test
    fun `falseを渡すとskipFirstLapがfalseとして保存される`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = true)
        val useCase = SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        useCase(false)

        assertFalse(repository.observeSkipFirstLap().first())
    }
}
