@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository)

        useCase(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        useCase(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)

        coVerify(exactly = 1) {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        }
        coVerify(exactly = 1) {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)
        }
        confirmVerified(repository)
    }
}
