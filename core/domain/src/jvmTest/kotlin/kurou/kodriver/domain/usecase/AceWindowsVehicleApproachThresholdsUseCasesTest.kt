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
    initialThreshold: Double = 5.0,
): AceWindowsVehicleApproachPreferencesRepository {
    val threshold = MutableStateFlow(initialThreshold)
    every { repository.observeThresholdMeters() } returns threshold
    listOf(7.0).forEach { value ->
        coEvery { repository.saveThresholdMeters(value) } answers { threshold.update { value } }
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
    fun `observeThresholdMeters はリポジトリの設定を返す`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository, initialThreshold = 5.0)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            assertEquals(5.0, useCases.observeThresholdMeters().first())
            verify(exactly = 1) { repository.observeThresholdMeters() }
            confirmVerified(repository)
        }

    @Test
    fun `saveThresholdMeters は閾値を保存する`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = AceWindowsVehicleApproachThresholdsUseCases(repository)

            useCases.saveThresholdMeters(7.0)

            assertEquals(7.0, useCases.observeThresholdMeters().first())
            coVerify(exactly = 1) { repository.saveThresholdMeters(7.0) }
            verify(exactly = 1) { repository.observeThresholdMeters() }
            confirmVerified(repository)
        }
}
