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
    initialSustainedDuration: Int = 4,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachThresholdsPreferencesRepository>()
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

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialSustainedDuration = 4)
        val useCase = ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        assertEquals(4, useCase().first())

        io.mockk.verify(exactly = 1) { repo.observeSustainedApproachDurationSeconds() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `保存済みの継続時間閾値をそのまま返す`() = runBlocking {
        val repo = createLmuWindowsVehicleApproachThresholdsPreferencesRepository(initialSustainedDuration = 4)
        val useCase = ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(repo)

        repo.saveSustainedApproachDurationSeconds(8)
        assertEquals(8, useCase().first())

        io.mockk.coVerify(exactly = 1) { repo.saveSustainedApproachDurationSeconds(8) }
        io.mockk.verify(exactly = 1) { repo.observeSustainedApproachDurationSeconds() }
        io.mockk.confirmVerified(repo)
    }
}
