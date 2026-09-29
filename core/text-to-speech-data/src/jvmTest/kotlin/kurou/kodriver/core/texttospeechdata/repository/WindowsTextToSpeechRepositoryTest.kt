package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason

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
                WindowsTextToSpeechRepository(FakeWindowsSpeechSynthesizer(available = false)).unavailableReason(),
            )
        }

    @Test
    fun `speakはテキストとqueueをそのまま音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeWindowsSpeechSynthesizer()

            WindowsTextToSpeechRepository(synthesizer).speak("ベストラップ", queue = true)

            assertEquals(listOf("ベストラップ" to true), synthesizer.spokenTexts)
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
}
