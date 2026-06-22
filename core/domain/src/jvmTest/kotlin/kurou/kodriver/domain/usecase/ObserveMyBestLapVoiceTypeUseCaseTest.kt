package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを監視できる`() = runBlocking {
        val repository = FakeMyBestLapPreferencesRepository(
            initialVoiceType = MyBestLapVoiceType.CASUAL,
        )
        val useCase = ObserveMyBestLapVoiceTypeUseCase(repository)

        assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
    }
}
