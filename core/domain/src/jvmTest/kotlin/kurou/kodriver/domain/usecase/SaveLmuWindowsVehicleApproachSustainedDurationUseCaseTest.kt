package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    initialSustainedDuration: Int = 4,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
    val sustainedDuration = MutableStateFlow(initialSustainedDuration)
    every { repository.observeSustainedApproachDurationSeconds() } returns sustainedDuration
    coEvery {
        repository.saveSustainedApproachDurationSeconds(any())
    } answers { sustainedDuration.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachSustainedDurationUseCaseTest {

    @Test
    fun `保存した継続時間閾値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        useCase(8)
        assertEquals(8, repo.observeSustainedApproachDurationSeconds().first())
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        useCase(8)
        useCase(6)
        assertEquals(6, repo.observeSustainedApproachDurationSeconds().first())
    }
}
