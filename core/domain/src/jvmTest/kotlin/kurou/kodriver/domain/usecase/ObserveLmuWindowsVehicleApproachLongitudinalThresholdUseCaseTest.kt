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

class ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCaseTest {

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLongitudinal = 10.0)
        val useCase = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

        assertEquals(10.0, useCase().first())
    }

    @Test
    fun `保存済みの縦方向閾値をそのまま返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLongitudinal = 10.0)
        val useCase = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

        repo.saveLongitudinalThresholdMeters(50.0)
        assertEquals(50.0, useCase().first())
    }
}
