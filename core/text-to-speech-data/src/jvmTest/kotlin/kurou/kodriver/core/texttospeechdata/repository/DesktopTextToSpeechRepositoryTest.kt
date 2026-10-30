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
class DesktopTextToSpeechRepositoryTest {
    @Test
    fun `isAvailableは音声合成の利用可否をそのまま返す`() =
        runTest {
            assertTrue(DesktopTextToSpeechRepository(FakeSpeechSynthesizer()).isAvailable())
            assertFalse(
                DesktopTextToSpeechRepository(FakeSpeechSynthesizer(available = false)).isAvailable(),
            )
        }

    @Test
    fun `unavailableReasonは利用できる時はnullで利用できない時は案内理由を返す`() =
        runTest {
            assertNull(DesktopTextToSpeechRepository(FakeSpeechSynthesizer()).unavailableReason())
            assertEquals(
                TextToSpeechUnavailableReason.WindowsSpeechUnavailable,
                DesktopTextToSpeechRepository(FakeSpeechSynthesizer(available = false), isWindows = true)
                    .unavailableReason(),
            )
        }

    @Test
    fun `Windows以外ではWindows向けの利用不可理由を返さない`() =
        runTest {
            assertNull(
                DesktopTextToSpeechRepository(
                    FakeSpeechSynthesizer(available = false),
                    isWindows = false,
                ).unavailableReason(),
            )
        }

    @Test
    fun `利用できると判明した後は再判定しない`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()
            val repository = DesktopTextToSpeechRepository(synthesizer, isWindows = true)

            assertTrue(repository.isAvailable())
            assertTrue(repository.isAvailable())
            assertNull(repository.unavailableReason())

            assertEquals(1, synthesizer.isAvailableCallCount)
            assertEquals(1, synthesizer.warmUpCount)
        }

    @Test
    fun `利用できない間は読み上げの準備を行わない`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer(available = false)

            DesktopTextToSpeechRepository(synthesizer, isWindows = true).isAvailable()

            assertEquals(0, synthesizer.warmUpCount)
        }

    @Test
    fun `利用できない間は呼び出しごとに再判定する`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer(available = false)
            val repository = DesktopTextToSpeechRepository(synthesizer, isWindows = true)

            assertFalse(repository.isAvailable())
            assertEquals(TextToSpeechUnavailableReason.WindowsSpeechUnavailable, repository.unavailableReason())

            assertEquals(2, synthesizer.isAvailableCallCount)
        }

    @Test
    fun `speakはテキストとqueueをそのまま音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()

            DesktopTextToSpeechRepository(synthesizer).speak("ベストラップ", queue = true)

            assertEquals(listOf("ベストラップ" to true), synthesizer.spokenTexts)
            assertEquals(listOf(100), synthesizer.spokenVolumes)
            assertEquals(listOf(1.0f), synthesizer.spokenSpeeds)
            assertEquals(listOf(1.0f), synthesizer.spokenPitches)
            assertEquals(listOf(""), synthesizer.spokenVoiceIds)
        }

    @Test
    fun `speakは音量を0から100の範囲に丸めて音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()
            val repository = DesktopTextToSpeechRepository(synthesizer)

            repository.speak("ベストラップ", queue = false, volume = 40)
            repository.speak("ベストラップ", queue = false, volume = -5)
            repository.speak("ベストラップ", queue = false, volume = 150)

            assertEquals(listOf(40, 0, 100), synthesizer.spokenVolumes)
        }

    @Test
    fun `空白のみのテキストは音声合成へ渡さない`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()

            DesktopTextToSpeechRepository(synthesizer).speak("  ", queue = false)

            assertTrue(synthesizer.spokenTexts.isEmpty())
        }

    @Test
    fun `コルーチンをキャンセルするとブロック中の音声合成へ割り込む`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer(blockUntilInterrupted = true)

            val job = launch { DesktopTextToSpeechRepository(synthesizer).speak("ベストラップ", queue = false) }
            runCurrent()
            synthesizer.speakStarted.await()
            job.cancel()
            job.join()

            assertTrue(synthesizer.wasInterrupted)
        }

    @Test
    fun `speakは保存済み音声IDを音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()

            DesktopTextToSpeechRepository(synthesizer).speak("試聴", voiceId = "voice-a")

            assertEquals(listOf("voice-a"), synthesizer.spokenVoiceIds)
        }

    @Test
    fun `speakは保存済み速度を渡し範囲外は上下限に丸める`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()
            val repository = DesktopTextToSpeechRepository(synthesizer)

            repository.speak("試聴", speed = 1.5f)
            repository.speak("試聴", speed = 0.1f)
            repository.speak("試聴", speed = 3.0f)

            assertEquals(listOf(1.5f, 0.5f, 2.0f), synthesizer.spokenSpeeds)
        }

    @Test
    fun `speakはピッチを上下限に制限して音声合成へ渡す`() =
        runTest {
            val synthesizer = FakeSpeechSynthesizer()
            val repository = DesktopTextToSpeechRepository(synthesizer)

            repository.speak("試聴", pitch = 1.5f)
            repository.speak("試聴", pitch = 0.1f)
            repository.speak("試聴", pitch = 3.0f)

            assertEquals(listOf(1.5f, 0.5f, 2.0f), synthesizer.spokenPitches)
        }
}
