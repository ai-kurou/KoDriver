package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class SaveLmuWindowsMyBestLapEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = FakeLmuWindowsMyBestLapEnabledRepository()
        val saveUseCase = SaveLmuWindowsMyBestLapEnabledUseCase(repository)
        val observeUseCase = ObserveLmuWindowsMyBestLapEnabledUseCase(repository)

        saveUseCase(true)

        assertTrue(observeUseCase().first())
    }
}
