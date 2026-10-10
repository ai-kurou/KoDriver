package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `車両クラスごとに任意の値を保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(repository)

            useCase(LmuWindowsVehicleClassData.Gte, 25)
            useCase(LmuWindowsVehicleClassData.Gt3, 15)

            coVerify(exactly = 1) {
                repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)
            }
            coVerify(exactly = 1) {
                repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gt3, 15)
            }
            confirmVerified(repository)
        }
}
