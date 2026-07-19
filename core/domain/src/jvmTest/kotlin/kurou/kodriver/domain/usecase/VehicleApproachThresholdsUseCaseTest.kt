package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    initialLongitudinal: Double = 10.0,
    initialLateral: Double = 5.0,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
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

    @Test
    fun `縦方向閾値を保存するとFlowに反映され上書きで更新される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val save = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
        val observe = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)

        save(50.0)
        assertEquals(50.0, observe().first())

        save(30.0)
        assertEquals(30.0, observe().first())
        io.mockk.coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(50.0) }
        io.mockk.coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(30.0) }
        io.mockk.verify(exactly = 2) { repo.observeLongitudinalThresholdMeters() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `横方向閾値を保存するとFlowに反映され上書きで更新される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val save = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)
        val observe = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        save(3.5)
        assertEquals(3.5, observe().first())

        save(1.0)
        assertEquals(1.0, observe().first())
        io.mockk.coVerify(exactly = 1) { repo.saveLateralThresholdMeters(3.5) }
        io.mockk.coVerify(exactly = 1) { repo.saveLateralThresholdMeters(1.0) }
        io.mockk.verify(exactly = 2) { repo.observeLateralThresholdMeters() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `縦横の閾値は独立して保持される`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val saveLongitudinal = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
        val saveLateral = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)
        val observeLongitudinal = ObserveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repo)
        val observeLateral = ObserveLmuWindowsVehicleApproachLateralThresholdUseCase(repo)

        saveLongitudinal(40.0)
        saveLateral(5.0)

        assertEquals(40.0, observeLongitudinal().first())
        assertEquals(5.0, observeLateral().first())
        io.mockk.coVerify(exactly = 1) { repo.saveLongitudinalThresholdMeters(40.0) }
        io.mockk.coVerify(exactly = 1) { repo.saveLateralThresholdMeters(5.0) }
        io.mockk.verify(exactly = 1) { repo.observeLongitudinalThresholdMeters() }
        io.mockk.verify(exactly = 1) { repo.observeLateralThresholdMeters() }
        io.mockk.confirmVerified(repo)
    }
}
