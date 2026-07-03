package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveLmuWindowsMyBestLapEnabledUseCaseTest {

    @Test
    fun `保存済みの有効状態を返す`() = runBlocking {
        val repository = FakeLmuWindowsMyBestLapEnabledRepository(initialEnabled = true)
        val useCase = ObserveLmuWindowsMyBestLapEnabledUseCase(repository)

        assertTrue(useCase().first())
    }
}
