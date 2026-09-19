@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleDamageEnabledStateUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)
            useCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)

            coVerify(
                exactly = 1,
            ) { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true) }
            coVerify(
                exactly = 1,
            ) { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false) }
            confirmVerified(repository)
        }
}
