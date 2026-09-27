package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `接近開始時読み上げ種別を保存できる`() =
        runTest {
            SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
            )

            coVerify(exactly = 1) {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            }
            confirmVerified(repository)
        }
}
