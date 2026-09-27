package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `選択したクラスを保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase(repository)

            useCase(LmuWindowsVehicleClassData.Gte)

            coVerify(exactly = 1) { repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(repository)
        }
}
