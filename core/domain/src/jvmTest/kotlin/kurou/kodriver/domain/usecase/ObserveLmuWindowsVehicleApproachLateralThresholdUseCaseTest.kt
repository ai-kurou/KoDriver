package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachLateralThresholdUseCaseTest {

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        assertEquals(2.0, useCase().first())
    }

    @Test
    fun `保存済みの横方向閾値をそのまま返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        repo.saveLateralThresholdMeters(3.5)
        assertEquals(3.5, useCase().first())
    }
}
