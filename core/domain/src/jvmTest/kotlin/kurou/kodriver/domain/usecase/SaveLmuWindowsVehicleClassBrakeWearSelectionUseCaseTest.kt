package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleClassBrakeWearSelectionUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeWearPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `選択したクラスを保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleClassBrakeWearSelectionUseCase(repository)

            useCase(LmuWindowsVehicleClassData.Gte)

            coVerify(exactly = 1) { repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(repository)
        }
}
