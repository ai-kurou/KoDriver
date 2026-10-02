package kurou.kodriver.domain.usecase

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAvailableVoicesUseCaseTest {
    @Test
    fun `日本語音声だけを表示名の昇順で返す`() =
        runTest {
            val first = TextToSpeechVoice("z-id", "A 音声", "ja-JP")
            val last = TextToSpeechVoice("a-id", "Z 音声", "ja-JP")
            val repository =
                FakeVoiceListRepository(
                    listOf(
                        last,
                        TextToSpeechVoice("en", "English", "en-US"),
                        first,
                        TextToSpeechVoice("ja", "日本語", "ja"),
                    ),
                )

            assertEquals(listOf(first, last), GetAvailableVoicesUseCase(repository)())
            assertEquals(1, repository.callCount)
        }

    @Test
    fun `空の一覧と日本語音声のない一覧は空を返す`() =
        runTest {
            assertEquals(emptyList(), GetAvailableVoicesUseCase(FakeVoiceListRepository(emptyList()))())
            val repository = FakeVoiceListRepository(listOf(TextToSpeechVoice("en", "English", "en-US")))
            assertEquals(emptyList(), GetAvailableVoicesUseCase(repository)())
        }

    private class FakeVoiceListRepository(
        private val voices: List<TextToSpeechVoice>,
    ) : VoiceListRepository {
        var callCount = 0
            private set

        override suspend fun availableVoices(): List<TextToSpeechVoice> {
            callCount++
            return voices
        }
    }
}
