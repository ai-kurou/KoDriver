package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCaseTest {

    @Test
    fun `invoke はリポジトリの接近開始時読み上げ設定を返す`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository(initialStartReadoutEnabled = false)
        val useCase = ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)

        assertEquals(false, useCase().first())
    }
}
