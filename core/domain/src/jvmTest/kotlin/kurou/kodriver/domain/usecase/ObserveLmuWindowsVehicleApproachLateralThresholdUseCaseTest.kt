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

class ObserveLmuWindowsVehicleApproachLateralThresholdUseCaseTest {

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        assertEquals(2.0, useCase().first())
    }

    @Test
    fun `保存済みの横方向閾値をそのまま返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        repo.saveLateralThresholdMeters(3.5)
        assertEquals(3.5, useCase().first())
    }
}
