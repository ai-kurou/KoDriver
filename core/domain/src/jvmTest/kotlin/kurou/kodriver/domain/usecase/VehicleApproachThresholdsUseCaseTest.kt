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
    initialLongitudinal: Double = 10.0,
    initialLateral: Double = 5.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val longitudinal = MutableStateFlow(initialLongitudinal)
    val lateral = MutableStateFlow(initialLateral)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    every { repository.observeLateralThresholdMeters() } returns lateral
    listOf(40.0, 50.0, 30.0).forEach { threshold ->
        coEvery { repository.saveLongitudinalThresholdMeters(threshold) } answers { longitudinal.update { threshold } }
    }
    listOf(5.0, 3.5, 1.0).forEach { threshold ->
        coEvery { repository.saveLateralThresholdMeters(threshold) } answers { lateral.update { threshold } }
    }
    return repository
}

class VehicleApproachThresholdsUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `縦方向閾値を保存するとFlowに反映され上書きで更新される`() =
        runTest {
            val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(repository)
            val save = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
            val observe = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

            save(50.0)
            assertEquals(50.0, observe().first())

            save(30.0)
            assertEquals(30.0, observe().first())
            coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(50.0) }
            coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(30.0) }
            verify(exactly = 2) { repo.observeLongitudinalThresholdMeters() }
            confirmVerified(repo)
        }

    @Test
    fun `横方向閾値を保存するとFlowに反映され上書きで更新される`() =
        runTest {
            val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(repository)
            val save = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)
            val observe = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

            save(3.5)
            assertEquals(3.5, observe().first())

            save(1.0)
            assertEquals(1.0, observe().first())
            coVerify(exactly = 1) { repo.saveLateralThresholdMeters(3.5) }
            coVerify(exactly = 1) { repo.saveLateralThresholdMeters(1.0) }
            verify(exactly = 2) { repo.observeLateralThresholdMeters() }
            confirmVerified(repo)
        }

    @Test
    fun `縦横の閾値は独立して保持される`() =
        runTest {
            val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(repository)
            val saveLongitudinal = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
            val saveLateral = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)
            val observeLongitudinal = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
            val observeLateral = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

            saveLongitudinal(40.0)
            saveLateral(5.0)

            assertEquals(40.0, observeLongitudinal().first())
            assertEquals(5.0, observeLateral().first())
            coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(40.0) }
            coVerify(exactly = 1) { repo.saveLateralThresholdMeters(5.0) }
            verify(exactly = 1) { repo.observeLongitudinalThresholdMeters() }
            verify(exactly = 1) { repo.observeLateralThresholdMeters() }
            confirmVerified(repo)
        }
}
