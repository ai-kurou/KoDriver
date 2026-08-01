@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleDamageEnabledStateUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleDamagePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runBlocking {
        val useCase = SaveLmuWindowsVehicleDamageEnabledStateUseCase(repository)

        useCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)
        useCase(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)

        coVerify(exactly = 1) { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true) }
        coVerify(exactly = 1) { repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false) }
        confirmVerified(repository)
    }
}
