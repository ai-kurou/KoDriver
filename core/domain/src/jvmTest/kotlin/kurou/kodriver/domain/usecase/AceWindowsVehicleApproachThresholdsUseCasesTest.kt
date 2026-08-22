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
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createAceWindowsVehicleApproachPreferencesRepository(
    repository: AceWindowsVehicleApproachPreferencesRepository,
    initialLongitudinal: Double = 5.0,
    initialLateral: Double = 5.0,
): AceWindowsVehicleApproachPreferencesRepository {
    val longitudinal = MutableStateFlow(initialLongitudinal)
    val lateral = MutableStateFlow(initialLateral)
    every { repository.observeLongitudinalThresholdMeters() } returns longitudinal
    listOf(7.0, 8.0).forEach { threshold ->
        coEvery { repository.saveLongitudinalThresholdMeters(threshold) } answers { longitudinal.update { threshold } }
    }
    every { repository.observeLateralThresholdMeters() } returns lateral
    listOf(6.0, 3.0).forEach { threshold ->
        coEvery { repository.saveLateralThresholdMeters(threshold) } answers { lateral.update { threshold } }
    }
    return repository
}

class AceWindowsVehicleApproachThresholdsUseCasesTest {
    @MockK
    private lateinit var repository: AceWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `observeLongitudinalThresholdMeters はリポジトリの設定を返す`() =
        runTest {
            val repository =
                createAceWindowsVehicleApproachPreferencesRepository(repository, initialLongitudinal = 5.0)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            assertEquals(5.0, useCases.observeLongitudinalThresholdMeters().first())
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            confirmVerified(repository)
        }

    @Test
    fun `saveLongitudinalThresholdMeters は前後方向閾値を保存する`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            useCases.saveLongitudinalThresholdMeters(7.0)

            assertEquals(7.0, useCases.observeLongitudinalThresholdMeters().first())
            coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(7.0) }
            verify(exactly = 1) { repository.observeLongitudinalThresholdMeters() }
            confirmVerified(repository)
        }

    @Test
    fun `observeLateralThresholdMeters はリポジトリの設定を返す`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository, initialLateral = 5.0)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            assertEquals(5.0, useCases.observeLateralThresholdMeters().first())
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            confirmVerified(repository)
        }

    @Test
    fun `saveLateralThresholdMeters は左右方向閾値を保存する`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            useCases.saveLateralThresholdMeters(6.0)

            assertEquals(6.0, useCases.observeLateralThresholdMeters().first())
            coVerify(exactly = 1) { repository.saveLateralThresholdMeters(6.0) }
            verify(exactly = 1) { repository.observeLateralThresholdMeters() }
            confirmVerified(repository)
        }
}
