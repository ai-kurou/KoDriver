package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveVehicleApproachStartReadoutTypeUseCaseTest {

    @Test
    fun `接近開始時読み上げ種別を保存できる`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository()
        val saveUseCase = SaveVehicleApproachStartReadoutTypeUseCase(repository)
        val observeUseCase = ObserveVehicleApproachStartReadoutTypeUseCase(repository)

        saveUseCase(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, observeUseCase().first())
    }
}
