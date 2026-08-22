@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsVehicleApproachEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: AceWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAceWindowsVehicleApproachEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            useCase(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, true)
            }
            confirmVerified(repository)
        }
}
