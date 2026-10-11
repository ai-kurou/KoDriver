package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsVehicleApproachEnabledStateUseCaseTest {
    private val repository: AceWindowsVehicleApproachPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAceWindowsVehicleApproachEnabledStateUseCase(repository)

            useCase(AceWindowsReadoutItemKey.VehicleApproach.StartReadout, false)
            useCase(AceWindowsReadoutItemKey.VehicleApproach.StartReadout, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(AceWindowsReadoutItemKey.VehicleApproach.StartReadout, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(AceWindowsReadoutItemKey.VehicleApproach.StartReadout, true)
            }
            confirmVerified(repository)
        }
}
