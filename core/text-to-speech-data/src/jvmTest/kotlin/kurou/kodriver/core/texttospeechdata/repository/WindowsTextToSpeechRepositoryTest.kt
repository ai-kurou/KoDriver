package kurou.kodriver.core.texttospeechdata.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
