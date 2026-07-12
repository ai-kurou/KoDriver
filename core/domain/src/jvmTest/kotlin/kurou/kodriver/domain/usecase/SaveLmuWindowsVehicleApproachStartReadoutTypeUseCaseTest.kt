package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    initialStartReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
): LmuWindowsVehicleApproachPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
    val state = MutableStateFlow(initialStartReadoutType)
    every { repository.observeStartReadoutType() } returns state
    coEvery { repository.saveStartReadoutType(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachStartReadoutTypeUseCaseTest {

    @Test
    fun `接近開始時読み上げ種別を保存できる`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository()
        val saveUseCase = SaveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)
        val observeUseCase = ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase(repository)

        saveUseCase(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, observeUseCase().first())
    }
}
