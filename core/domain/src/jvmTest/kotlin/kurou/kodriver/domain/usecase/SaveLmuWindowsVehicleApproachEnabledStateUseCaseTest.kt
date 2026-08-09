@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleApproachEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)
            useCase(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, true)
            }
            confirmVerified(repository)
        }
}
