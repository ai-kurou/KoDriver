package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `接近継続時読み上げ種別を監視できる`() =
        runTest {
            every { repository.observeSustainedReadoutType() } returns
                MutableStateFlow(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            val useCase = ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase(repository)

            assertEquals(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED, useCase().first())
            verify(exactly = 1) { repository.observeSustainedReadoutType() }
            confirmVerified(repository)
        }
}
