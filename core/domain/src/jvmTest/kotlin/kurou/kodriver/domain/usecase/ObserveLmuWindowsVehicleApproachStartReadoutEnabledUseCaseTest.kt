package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCaseTest {

    @Test
    fun `invoke はリポジトリの接近開始時読み上げ設定を返す`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
        every { repository.observeStartReadoutEnabled() } returns MutableStateFlow(false)
        val useCase = ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)

        assertEquals(false, useCase().first())
    }
}
