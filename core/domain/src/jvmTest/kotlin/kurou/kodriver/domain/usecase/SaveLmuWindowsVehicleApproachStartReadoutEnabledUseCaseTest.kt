package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    initialStartReadoutEnabled: Boolean = true,
): LmuWindowsVehicleApproachPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
    val state = MutableStateFlow(initialStartReadoutEnabled)
    every { repository.observeStartReadoutEnabled() } returns state
    coEvery { repository.saveStartReadoutEnabled(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveLmuWindowsVehicleApproachStartReadoutEnabledUseCaseTest {

    @Test
    fun `invoke は接近開始時読み上げ設定を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository()
        val saveUseCase = SaveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)
        val observeUseCase = ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)

        saveUseCase(false)

        assertEquals(false, observeUseCase().first())
    }
}
