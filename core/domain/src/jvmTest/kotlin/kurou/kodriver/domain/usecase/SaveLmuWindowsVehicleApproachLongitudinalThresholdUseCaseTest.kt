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
    initialLongitudinal: Double = 10.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
    val longitudinal = MutableStateFlow(initialLongitudinal)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    coEvery { repository.saveLongitudinalThresholdMeters(any()) } answers { longitudinal.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCaseTest {

    @Test
    fun `保存した縦方向閾値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

        useCase(50.0)
        assertEquals(50.0, repo.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

        useCase(50.0)
        useCase(30.0)
        assertEquals(30.0, repo.observeLongitudinalThresholdMeters().first())
    }
}
