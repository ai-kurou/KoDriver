package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5MyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを監視できる`() = runBlocking {
        val repository = FakeGt7Ps5MyBestLapPreferencesRepository(
            initialVoiceType = MyBestLapVoiceType.CASUAL,
        )
        val useCase = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(repository)

        assertEquals(MyBestLapVoiceType.CASUAL, useCase().first())
    }
}
