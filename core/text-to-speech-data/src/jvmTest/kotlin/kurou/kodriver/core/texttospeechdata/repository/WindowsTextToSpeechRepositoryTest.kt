package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WindowsTextToSpeechRepositoryTest {
    @Test
    fun `isAvailableは音声合成の利用可否をそのまま返す`() =
        runTest {
            assertTrue(WindowsTextToSpeechRepository(FakeWindowsSpeechSynthesizer()).isAvailable())
            assertFalse(
                WindowsTextToSpeechRepository(FakeWindowsSpeechSynthesizer(available = false)).isAvailable(),
            )
        }

    @Test
    fun `unavailableReasonは利用できる時はnullで利用できない時は案内理由を返す`() =
        runTest {
            assertNull(WindowsTextToSpeechRepository(FakeWindowsSpeechSynthesizer()).unavailableReason())
            assertEquals(
                TextToSpeechUnavailableReason.WindowsSpeechUnavailable,
                WindowsTextToSpeechRepository(FakeWindowsSpeechSynthesizer(available = false), isWindows = true)
                    .unavailableReason(),
            )
        }

    @Test
    fun `Windows以外ではWindows向けの利用不可理由を返さない`() =
        runTest {
            assertNull(
                WindowsTextToSpeechRepository(
                    FakeWindowsSpeechSynthesizer(available = false),
                    isWindows = false,
                ).unavailableReason(),
            )
        }

    @Test
    fun `利用できると判明した後は再判定しない`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()
            val repository = WindowsTextToSpeechRepository(synthesizer, isWindows = true)

            assertTrue(repository.isAvailable())
            assertTrue(repository.isAvailable())
            assertNull(repository.unavailableReason())

            assertEquals(1, synthesizer.isAvailableCallCount)
        }

    @Test
    fun `利用できない間は呼び出しごとに再判定する`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer(available = false)
            val repository = WindowsTextToSpeechRepository(synthesizer, isWindows = true)

            assertFalse(repository.isAvailable())
            assertEquals(TextToSpeechUnavailableReason.WindowsSpeechUnavailable, repository.unavailableReason())

            assertEquals(2, synthesizer.isAvailableCallCount)
        }

    @Test
    fun `speakはテキストとqueueをそのまま音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()

            WindowsTextToSpeechRepository(synthesizer).speak("ベストラップ", queue = true)

            assertEquals(listOf("ベストラップ" to true), synthesizer.spokenTexts)
            assertEquals(listOf(100), synthesizer.spokenVolumes)
            assertEquals(listOf(""), synthesizer.spokenVoiceIds)
        }

    @Test
    fun `speakは音量を0から100の範囲に丸めて音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()
            val repository = WindowsTextToSpeechRepository(synthesizer)

            repository.speak("ベストラップ", queue = false, volume = 40)
            repository.speak("ベストラップ", queue = false, volume = -5)
            repository.speak("ベストラップ", queue = false, volume = 150)

            assertEquals(listOf(40, 0, 100), synthesizer.spokenVolumes)
        }

    @Test
    fun `空白のみのテキストは音声合成へ渡さない`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()

            WindowsTextToSpeechRepository(synthesizer).speak("  ", queue = false)

            assertTrue(synthesizer.spokenTexts.isEmpty())
        }

    @Test
    fun `stopは音声合成の停止を呼び出す`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()

            WindowsTextToSpeechRepository(synthesizer).stop()

            assertEquals(1, synthesizer.stopCount)
        }

    @Test
    fun `コルーチンをキャンセルするとブロック中の音声合成へ割り込む`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer(blockUntilInterrupted = true)

            val job = launch { WindowsTextToSpeechRepository(synthesizer).speak("ベストラップ", queue = false) }
            runCurrent()
            synthesizer.speakStarted.await()
            job.cancel()
            job.join()

            assertTrue(synthesizer.wasInterrupted)
        }

    @Test
    fun `speakは保存済み音声IDを音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()

            WindowsTextToSpeechRepository(synthesizer).speak("試聴", voiceId = "voice-a")

            assertEquals(listOf("voice-a"), synthesizer.spokenVoiceIds)
        }
}
