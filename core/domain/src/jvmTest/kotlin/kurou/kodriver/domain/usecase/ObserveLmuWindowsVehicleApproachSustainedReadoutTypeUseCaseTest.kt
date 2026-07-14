package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCaseTest {

    @Test
    fun `接近継続時読み上げ種別を監視できる`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
        every { repository.observeSustainedReadoutType() } returns
            MutableStateFlow(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
        val useCase = ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase(repository)

        assertEquals(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED, useCase().first())
    }
}
