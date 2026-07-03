package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `保存済みのLMU自己ベストラップ音声タイプを返す`() = runBlocking {
        val repository = FakeLmuWindowsMyBestLapPreferencesRepository(
            initialVoiceType = MyBestLapVoiceType.CASUAL,
        )
        val useCase = ObserveLmuWindowsMyBestLapVoiceTypeUseCase(repository)

        assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
    }
}
