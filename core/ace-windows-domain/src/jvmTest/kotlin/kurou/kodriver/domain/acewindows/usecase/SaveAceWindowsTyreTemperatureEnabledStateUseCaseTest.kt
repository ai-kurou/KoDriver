package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureEnabledStateUseCaseTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerAceWindowsReadoutItemKeys()
    }

    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveAceWindowsTyreTemperatureEnabledStateUseCase(repository)

            useCase(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)
            useCase(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)
            }
            confirmVerified(repository)
        }
}
