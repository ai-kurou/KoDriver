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
    initialSustainedEnabled: Boolean = true,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
    val longitudinal = MutableStateFlow(initialLongitudinal)
    val lateral = MutableStateFlow(initialLateral)
    val sustainedDuration = MutableStateFlow(initialSustainedDuration)
    val sustainedEnabled = MutableStateFlow(initialSustainedEnabled)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    coEvery { repository.saveLongitudinalThresholdMeters(any()) } answers { longitudinal.update { firstArg() } }
    every { repository.observeLateralThresholdMeters() } returns lateral
    coEvery { repository.saveLateralThresholdMeters(any()) } answers { lateral.update { firstArg() } }
    every { repository.observeSustainedApproachDurationSeconds() } returns sustainedDuration
    coEvery {
        repository.saveSustainedApproachDurationSeconds(any())
    } answers { sustainedDuration.update { firstArg() } }
    every { repository.observeSustainedApproachEnabled() } returns sustainedEnabled
    coEvery {
        repository.saveSustainedApproachEnabled(any())
    } answers { sustainedEnabled.update { firstArg() } }
    return repository
}

class LmuWindowsVehicleApproachThresholdsUseCasesTest {

    @Test
    fun `observeLongitudinalThresholdMeters はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLongitudinal = 10.0)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(10.0, useCases.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `saveLongitudinalThresholdMeters は縦方向閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveLongitudinalThresholdMeters(50.0)

        assertEquals(50.0, useCases.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `observeLateralThresholdMeters はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialLateral = 5.0)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(5.0, useCases.observeLateralThresholdMeters().first())
    }

    @Test
    fun `saveLateralThresholdMeters は横方向閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveLateralThresholdMeters(3.5)

        assertEquals(3.5, useCases.observeLateralThresholdMeters().first())
    }

    @Test
    fun `observeSustainedApproachDurationSeconds はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialSustainedDuration = 4)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(4, useCases.observeSustainedApproachDurationSeconds().first())
    }

    @Test
    fun `saveSustainedApproachDurationSeconds は継続時間閾値を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveSustainedApproachDurationSeconds(8)

        assertEquals(8, useCases.observeSustainedApproachDurationSeconds().first())
    }

    @Test
    fun `observeSustainedApproachEnabled はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialSustainedEnabled = true)
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        assertEquals(true, useCases.observeSustainedApproachEnabled().first())
    }

    @Test
    fun `saveSustainedApproachEnabled は継続読み上げ有効フラグを保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachThresholdsPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachThresholdsUseCases(repository)

        useCases.saveSustainedApproachEnabled(false)

        assertEquals(false, useCases.observeSustainedApproachEnabled().first())
    }
}
