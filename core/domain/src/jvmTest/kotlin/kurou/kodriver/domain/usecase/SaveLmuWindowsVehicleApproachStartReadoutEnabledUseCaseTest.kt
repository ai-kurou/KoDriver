package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsVehicleApproachStartReadoutEnabledUseCaseTest {

    @Test
    fun `invoke は接近開始時読み上げ設定を保存する`() = runBlocking {
        val repository = FakeLmuWindowsVehicleApproachPreferencesRepository()
        val saveUseCase = SaveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)
        val observeUseCase = ObserveLmuWindowsVehicleApproachStartReadoutEnabledUseCase(repository)

        saveUseCase(false)

        assertEquals(false, observeUseCase().first())
    }
}
