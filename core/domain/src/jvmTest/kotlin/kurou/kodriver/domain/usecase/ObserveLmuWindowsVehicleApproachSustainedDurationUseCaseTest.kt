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
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
    initialSustainedDuration: Int = 4,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val sustainedDuration = MutableStateFlow(initialSustainedDuration)
    every { repository.observeSustainedApproachDurationSeconds() } returns sustainedDuration
    coEvery {
        repository.saveSustainedApproachDurationSeconds(8)
    } answers { sustainedDuration.update { 8 } }
    coEvery {
        repository.saveSustainedApproachDurationSeconds(6)
    } answers { sustainedDuration.update { 6 } }
    return repository
}

class ObserveLmuWindowsVehicleApproachSustainedDurationUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す`() =
        runBlocking {
        val repo =
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
            repository,
            initialSustainedDuration = 4,
        )
        val useCase = ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        assertEquals(4, useCase().first())

        verify(exactly = 1) { repo.observeSustainedApproachDurationSeconds() }
        confirmVerified(repo)
    }

    @Test
    fun `保存済みの継続時間閾値をそのまま返す`() =
        runBlocking {
        val repo =
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
            repository,
            initialSustainedDuration = 4,
        )
        val useCase = ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        repo.saveSustainedApproachDurationSeconds(8)
        assertEquals(8, useCase().first())

        coVerify(exactly = 1) { repo.saveSustainedApproachDurationSeconds(8) }
        verify(exactly = 1) { repo.observeSustainedApproachDurationSeconds() }
        confirmVerified(repo)
    }
}
