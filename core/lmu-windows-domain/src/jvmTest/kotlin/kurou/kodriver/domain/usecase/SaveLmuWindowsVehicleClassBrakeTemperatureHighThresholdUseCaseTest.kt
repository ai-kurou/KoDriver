package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `車両クラスごとに任意の値を保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(repository)

            useCase(LmuWindowsVehicleClassData.Gte, 800)
            useCase(LmuWindowsVehicleClassData.Gt3, 650)

            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 650)
            }
            confirmVerified(repository)
        }
}
