package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {

    @Test
    fun `接近開始時読み上げ種別を保存できる`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)(
            VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
        )

        coVerify(exactly = 1) {
            repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
        }
        confirmVerified(repository)
    }
}
