package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveGt7Ps5MyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを保存できる`() = runBlocking {
        val repository = FakeGt7Ps5MyBestLapPreferencesRepository()
        val saveUseCase = SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository)
        val observeUseCase = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository)

        saveUseCase(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, observeUseCase().first())
    }
}
