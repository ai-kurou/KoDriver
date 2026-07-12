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
    initialLateral: Double = 5.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
    val lateral = MutableStateFlow(initialLateral)
    every { repository.observeLateralThresholdMeters() } returns lateral
    coEvery { repository.saveLateralThresholdMeters(any()) } answers { lateral.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachLateralThresholdUseCaseTest {

    @Test
    fun `保存した横方向閾値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        useCase(3.5)
        assertEquals(3.5, repo.observeLateralThresholdMeters().first())
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCase = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        useCase(3.5)
        useCase(1.0)
        assertEquals(1.0, repo.observeLateralThresholdMeters().first())
    }
}
