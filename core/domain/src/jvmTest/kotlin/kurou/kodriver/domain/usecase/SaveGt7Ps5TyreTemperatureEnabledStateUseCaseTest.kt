@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveGt7Ps5TyreTemperatureEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5TyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveGt7Ps5TyreTemperatureEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            useCase(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning, true)
            }
            confirmVerified(repository)
        }
}
