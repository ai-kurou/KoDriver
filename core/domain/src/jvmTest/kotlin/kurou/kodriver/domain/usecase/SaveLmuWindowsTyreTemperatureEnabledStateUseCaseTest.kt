@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureEnabledStateUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
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
