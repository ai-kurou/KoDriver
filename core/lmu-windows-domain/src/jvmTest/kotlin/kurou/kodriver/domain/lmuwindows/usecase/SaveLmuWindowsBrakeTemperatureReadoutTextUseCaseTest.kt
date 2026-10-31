package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsBrakeTemperatureReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)("  温度{celsius}℃  ")

            coVerify(exactly = 1) { repository.saveReadoutText("温度{celsius}℃") }
            confirmVerified(repository)
        }
}
