package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveVehicleApproachStartReadoutTypeUseCaseTest {

    @Test
    fun `接近開始時読み上げ種別を監視できる`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(
            initialStartReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
        )
        val useCase = ObserveVehicleApproachStartReadoutTypeUseCase(repository)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCase().first())
    }
}
