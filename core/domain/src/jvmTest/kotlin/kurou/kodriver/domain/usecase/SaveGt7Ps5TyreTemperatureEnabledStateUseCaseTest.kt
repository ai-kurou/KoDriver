package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5TyreTemperatureEnabledStateUseCaseTest {
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveGt7Ps5TyreTemperatureEnabledStateUseCase(repository)

            useCase(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, false)
            useCase(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, true)
            }
            confirmVerified(repository)
        }
}
