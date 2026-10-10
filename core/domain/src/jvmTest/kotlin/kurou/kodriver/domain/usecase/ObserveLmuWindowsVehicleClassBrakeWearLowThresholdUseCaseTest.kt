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
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCaseTest {
    private val repo: LmuWindowsVehicleClassBrakeWearPreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state =
                MutableStateFlow<Map<LmuWindowsVehicleClassData, Int>>(
                    mapOf(LmuWindowsVehicleClassData.Gte to 35),
                )
            every { repo.observeLowThresholdPercent() } returns state
            coEvery { repo.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25) } answers {
                state.update { it + (LmuWindowsVehicleClassData.Gte to 25) }
            }
            val useCase = ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(repo)

            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 35),
                useCase().first(),
            )

            repo.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)
            assertEquals<Map<LmuWindowsVehicleClassData, Int>>(
                mapOf(LmuWindowsVehicleClassData.Gte to 25),
                useCase().first(),
            )

            verify(exactly = 2) { repo.observeLowThresholdPercent() }
            coVerify(exactly = 1) {
                repo.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)
            }
            confirmVerified(repo)
        }
}
