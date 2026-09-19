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
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    initialLongitudinal: Double = 10.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val longitudinal = MutableStateFlow(initialLongitudinal)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    listOf(40.0, 50.0, 30.0).forEach { threshold ->
        coEvery { repository.saveLongitudinalThresholdMeters(threshold) } answers { longitudinal.update { threshold } }
    }
    return repository
}

class ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCaseTest {
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository = mockk()

    @Test
    fun `初期値を返す`() =
        runTest {
            val repo =
                createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
                    repository,
                    initialLongitudinal = 10.0,
                )
            val useCase = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

            assertEquals(10.0, useCase().first())

            verify(exactly = 1) { repo.observeLongitudinalThresholdMeters() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの縦方向閾値をそのまま返す`() =
        runTest {
            val repo =
                createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
                    repository,
                    initialLongitudinal = 10.0,
                )
            val useCase = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

            repo.saveLongitudinalThresholdMeters(50.0)
            assertEquals(50.0, useCase().first())

            coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(50.0) }
            verify(exactly = 1) { repo.observeLongitudinalThresholdMeters() }
            confirmVerified(repo)
        }
}
