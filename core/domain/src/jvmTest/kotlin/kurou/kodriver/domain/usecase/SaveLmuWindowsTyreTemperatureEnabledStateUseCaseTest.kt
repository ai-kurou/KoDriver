package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureEnabledStateUseCaseTest {
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsTyreTemperatureEnabledStateUseCase(repository)

            useCase(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)
            useCase(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)
            }
            confirmVerified(repository)
        }
}
