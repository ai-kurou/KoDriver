package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kotlin.test.Test

class SpeakTextUseCaseTest {
    private val repository: TextToSpeechRepository = mockk()
    private val observeVoice: ObserveVoiceUseCase = mockk()
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase = mockk()

    @Test
    fun `明示した音声でも保存済み速度をRepositoryへ渡す`() =
        runTest {
            every { observeVoiceSpeed() } returns flowOf(1.5f)
            coEvery { repository.speak("試聴", false, 42, "voice-b", 1.5f) } returns Unit
            coEvery { repository.speak("試聴", false, 42, VOICE_ID_UNSPECIFIED, 1.5f) } returns Unit

            val speakText = SpeakTextUseCase(repository, observeVoice, observeVoiceSpeed)
            speakText("試聴", volume = 42, voiceId = "voice-b")
            speakText("試聴", volume = 42, voiceId = VOICE_ID_UNSPECIFIED)

            verify(exactly = 0) { observeVoice() }
            coVerify(exactly = 1) { repository.speak("試聴", false, 42, "voice-b", 1.5f) }
            coVerify(exactly = 1) { repository.speak("試聴", false, 42, VOICE_ID_UNSPECIFIED, 1.5f) }
            verify(exactly = 2) { observeVoiceSpeed() }
            confirmVerified(repository, observeVoice, observeVoiceSpeed)
        }

    @Test
    fun `保存済み音声とテキストとqueueと音量と保存済み速度をRepositoryへ渡す`() =
        runTest {
            every { observeVoiceSpeed() } returns flowOf(1.5f)
            every { observeVoice() } returns flowOf("voice-a")
            coEvery { repository.speak("ベストラップ", true, 30, "voice-a", 1.5f) } returns Unit

            SpeakTextUseCase(repository, observeVoice, observeVoiceSpeed)("ベストラップ", queue = true, volume = 30)

            verify(exactly = 1) { observeVoice() }
            coVerify(exactly = 1) { repository.speak("ベストラップ", true, 30, "voice-a", 1.5f) }
            verify(exactly = 1) { observeVoiceSpeed() }
            confirmVerified(repository, observeVoice, observeVoiceSpeed)
        }

    @Test
    fun `音声未指定の場合も既定の引数と音声IDをRepositoryへ渡す`() =
        runTest {
            every { observeVoiceSpeed() } returns flowOf(1.0f)
            every { observeVoice() } returns flowOf(VOICE_ID_UNSPECIFIED)
            coEvery { repository.speak("ベストラップ", false, 100, VOICE_ID_UNSPECIFIED, 1.0f) } returns Unit

            SpeakTextUseCase(repository, observeVoice, observeVoiceSpeed)("ベストラップ")

            verify(exactly = 1) { observeVoice() }
            coVerify(exactly = 1) { repository.speak("ベストラップ", false, 100, VOICE_ID_UNSPECIFIED, 1.0f) }
            verify(exactly = 1) { observeVoiceSpeed() }
            confirmVerified(repository, observeVoice, observeVoiceSpeed)
        }

    @Test
    fun `空文字と空白のみのテキストは音声と速度設定を取得せず読み上げない`() =
        runTest {
            val speakText = SpeakTextUseCase(repository, observeVoice, observeVoiceSpeed)
            speakText("")
            speakText("  ")

            verify(exactly = 0) { observeVoice() }
            coVerify(exactly = 0) { repository.speak("", false, 100, VOICE_ID_UNSPECIFIED, 1.0f) }
            coVerify(exactly = 0) { repository.speak("  ", false, 100, VOICE_ID_UNSPECIFIED, 1.0f) }
            verify(exactly = 0) { observeVoiceSpeed() }
            confirmVerified(repository, observeVoice, observeVoiceSpeed)
        }
}
