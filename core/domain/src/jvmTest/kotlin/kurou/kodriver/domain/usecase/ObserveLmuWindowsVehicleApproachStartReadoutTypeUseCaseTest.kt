package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接近開始時読み上げ種別を監視できる`() =
        runBlocking {
        every { repository.observeStartReadoutType() } returns
            MutableStateFlow(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
        val useCase = ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCase().first())
        verify(exactly = 1) { repository.observeStartReadoutType() }
        confirmVerified(repository)
    }
}
