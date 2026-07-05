package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class SaveLmuWindowsTyreTemperatureEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = FakeLmuWindowsTyreTemperatureEnabledRepository()
        val saveUseCase = SaveLmuWindowsTyreTemperatureEnabledUseCase(repository)
        val observeUseCase = ObserveLmuWindowsTyreTemperatureEnabledUseCase(repository)

        saveUseCase(true)

        assertTrue(observeUseCase().first())
    }
}
