package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCaseTest {
    private val repo: LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(
                    mapOf(LmuWindowsVehicleClassData.Gte to 700),
                )
            every { repo.observeHighThresholdCelsius() } returns state
            coEvery { repo.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800) } answers {
                state.update { it + (LmuWindowsVehicleClassData.Gte to 800) }
            }
            val useCase = ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(repo)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 700),
                useCase().first(),
            )

            repo.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 800),
                useCase().first(),
            )

            verify(exactly = 2) { repo.observeHighThresholdCelsius() }
            coVerify(exactly = 1) {
                repo.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)
            }
            confirmVerified(repo)
        }
}
