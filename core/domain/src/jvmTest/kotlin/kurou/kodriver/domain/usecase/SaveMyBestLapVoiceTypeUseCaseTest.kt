package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを保存できる`() = runBlocking {
        val repository = FakeMyBestLapPreferencesRepository()
        val saveUseCase = SaveMyBestLapVoiceTypeUseCase(repository)
        val observeUseCase = ObserveMyBestLapVoiceTypeUseCase(repository)

        saveUseCase(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, observeUseCase().first())
    }
}
