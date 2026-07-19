package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCaseTest {

    @Test
    fun `縦方向閾値を保存するとFlowに反映され上書きで更新される`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repository)

        useCase(50.0)
        useCase(30.0)

        coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(50.0) }
        coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(30.0) }
        confirmVerified(repository)
    }
}
