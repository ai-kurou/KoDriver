package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachEnabledStateUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleApproachEnabledStateUseCase(repository)

            useCase(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)
            useCase(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, true)
            }
            confirmVerified(repository)
        }
}
