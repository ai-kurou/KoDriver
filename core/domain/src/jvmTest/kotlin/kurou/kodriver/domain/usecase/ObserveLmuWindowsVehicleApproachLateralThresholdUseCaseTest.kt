package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    initialLateral: Double = 5.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val lateral = MutableStateFlow(initialLateral)
    every { repository.observeLateralThresholdMeters() } returns lateral
    listOf(5.0, 3.5, 1.0).forEach { threshold ->
        coEvery { repository.saveLateralThresholdMeters(threshold) } answers { lateral.update { threshold } }
    }
    return repository
}

class ObserveLmuWindowsVehicleApproachLateralThresholdUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す`() =
        runTest {
            val repo =
                createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
                    repository,
                    initialLateral = 2.0,
                )
            val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

            assertEquals(2.0, useCase().first())
            verify(exactly = 1) { repo.observeLateralThresholdMeters() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの横方向閾値をそのまま返す`() =
        runTest {
            val repo =
                createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
                    repository,
                    initialLateral = 2.0,
                )
            val useCase = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

            repo.saveLateralThresholdMeters(3.5)
            assertEquals(3.5, useCase().first())
            coVerify(exactly = 1) { repo.saveLateralThresholdMeters(3.5) }
            verify(exactly = 1) { repo.observeLateralThresholdMeters() }
            confirmVerified(repo)
        }
}
