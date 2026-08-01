package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接近開始時読み上げ種別を保存できる`() =
        runBlocking {
            SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
            )

            coVerify(exactly = 1) {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            }
            confirmVerified(repository)
        }
}
