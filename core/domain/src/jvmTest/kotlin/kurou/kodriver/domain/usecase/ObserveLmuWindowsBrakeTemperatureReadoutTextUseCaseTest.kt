package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsBrakeTemperatureReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk()

    @Test
    fun `ブレーキ過熱読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeReadoutText() } returns MutableStateFlow("温度{celsius}℃")
            val useCase = ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)

            assertEquals("温度{celsius}℃", useCase().first())
            verify(exactly = 1) { repository.observeReadoutText() }
            confirmVerified(repository)
        }
}
