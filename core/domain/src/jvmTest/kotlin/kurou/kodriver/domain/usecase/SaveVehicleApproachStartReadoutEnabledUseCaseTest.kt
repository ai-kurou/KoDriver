package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveVehicleApproachStartReadoutEnabledUseCaseTest {

    @Test
    fun `invoke は接近開始時読み上げ設定を保存する`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository()
        val saveUseCase = SaveVehicleApproachStartReadoutEnabledUseCase(repository)
        val observeUseCase = ObserveVehicleApproachStartReadoutEnabledUseCase(repository)

        saveUseCase(false)

        assertEquals(false, observeUseCase().first())
    }
}
