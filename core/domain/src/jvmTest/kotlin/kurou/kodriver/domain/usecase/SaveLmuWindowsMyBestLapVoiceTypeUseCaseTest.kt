package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `LMU自己ベストラップ音声タイプを保存する`() = runBlocking {
        val repository = FakeLmuWindowsMyBestLapPreferencesRepository()
        val saveUseCase = SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository)
        val observeUseCase = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository)

        saveUseCase(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, observeUseCase().first())
    }
}
