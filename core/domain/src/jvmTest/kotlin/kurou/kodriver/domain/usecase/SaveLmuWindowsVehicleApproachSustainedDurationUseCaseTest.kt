package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSustainedDurationUseCaseTest {

    @Test
    fun `保存した継続時間閾値がFlowに反映される`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repository)(8)

        coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(8) }
        confirmVerified(repository)
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repository)

        useCase(8)
        useCase(6)

        coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(8) }
        coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(6) }
        confirmVerified(repository)
    }
}
