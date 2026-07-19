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
    initialSustainedDuration: Int = 4,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
    val longitudinal = MutableStateFlow(initialLongitudinal)
    val lateral = MutableStateFlow(initialLateral)
    val sustainedDuration = MutableStateFlow(initialSustainedDuration)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    listOf(40.0, 50.0, 30.0).forEach { threshold ->
        coEvery { repository.saveLongitudinalThresholdMeters(threshold) } answers { longitudinal.update { threshold } }
    }
    every { repository.observeLateralThresholdMeters() } returns lateral
    listOf(5.0, 3.5, 1.0).forEach { threshold ->
        coEvery { repository.saveLateralThresholdMeters(threshold) } answers { lateral.update { threshold } }
    }
    every { repository.observeSustainedApproachDurationSeconds() } returns sustainedDuration
    coEvery {
        repository.saveSustainedApproachDurationSeconds(8)
    } answers { sustainedDuration.update { 8 } }
    coEvery {
        repository.saveSustainedApproachDurationSeconds(6)
    } answers { sustainedDuration.update { 6 } }
    return repository
}

class LmuWindowsVehicleApproachThresholdsUseCasesTest {

    @Test
    fun `observeLongitudinalThresholdMeters はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLongitudinal = 10.0)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(10.0, useCases.observeLongitudinalThresholdMeters().first())
        io.mockk.verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
        io.mockk.confirmVerified(repository)
    }

    @Test
    fun `saveLongitudinalThresholdMeters は縦方向閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveLongitudinalThresholdMeters(50.0)

        assertEquals(50.0, useCases.observeLongitudinalThresholdMeters().first())
        io.mockk.coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(50.0) }
        io.mockk.verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
        io.mockk.confirmVerified(repository)
    }

    @Test
    fun `observeLateralThresholdMeters はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 5.0)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(5.0, useCases.observeLateralThresholdMeters().first())
        io.mockk.verify(exactly = 1) { repository.observeLateralThresholdMeters() }
        io.mockk.confirmVerified(repository)
    }

    @Test
    fun `saveLateralThresholdMeters は横方向閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveLateralThresholdMeters(3.5)

        assertEquals(3.5, useCases.observeLateralThresholdMeters().first())
        io.mockk.coVerify(exactly = 1) { repository.saveLateralThresholdMeters(3.5) }
        io.mockk.verify(exactly = 1) { repository.observeLateralThresholdMeters() }
        io.mockk.confirmVerified(repository)
    }

    @Test
    fun `observeSustainedApproachDurationSeconds はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialSustainedDuration = 4)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(4, useCases.observeSustainedApproachDurationSeconds().first())
        io.mockk.verify(exactly = 1) { repository.observeSustainedApproachDurationSeconds() }
        io.mockk.confirmVerified(repository)
    }

    @Test
    fun `saveSustainedApproachDurationSeconds は継続時間閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveSustainedApproachDurationSeconds(8)

        assertEquals(8, useCases.observeSustainedApproachDurationSeconds().first())
        io.mockk.coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(8) }
        io.mockk.verify(exactly = 1) { repository.observeSustainedApproachDurationSeconds() }
        io.mockk.confirmVerified(repository)
    }
}
