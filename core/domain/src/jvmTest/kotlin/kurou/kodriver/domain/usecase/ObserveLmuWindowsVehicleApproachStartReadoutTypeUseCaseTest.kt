package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `接近開始時読み上げ種別を監視できる`() =
        runTest {
            every { repository.observeStartReadoutType() } returns
                MutableStateFlow(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            val useCase = ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)

            assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCase().first())
            verify(exactly = 1) { repository.observeStartReadoutType() }
            confirmVerified(repository)
        }
}
