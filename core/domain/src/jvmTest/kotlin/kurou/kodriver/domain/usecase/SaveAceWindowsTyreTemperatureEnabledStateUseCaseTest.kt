@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureEnabledStateUseCaseTest {
    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAceWindowsTyreTemperatureEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            useCase(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, true)
            }
            confirmVerified(repository)
        }
}
