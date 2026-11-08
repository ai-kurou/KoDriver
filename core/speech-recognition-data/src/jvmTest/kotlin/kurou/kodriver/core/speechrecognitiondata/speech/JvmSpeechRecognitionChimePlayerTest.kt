package kurou.kodriver.core.speechrecognitiondata.speech

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.SourceDataLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JvmSpeechRecognitionChimePlayerTest {
    private val line: SourceDataLine = mockk()
    private val expectedBytes = wakeWordChimePcm().toLittleEndianBytes()
    private val expectedSize = expectedBytes.size

    @Test
    fun `出力ラインを開いて確認音を書き込み、鳴り終わるまで待って閉じる`() =
        runTest {
            every { line.open(match { it.isChimeFormat() }) } returns Unit
            every { line.start() } returns Unit
            every { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) } returns expectedSize
            every { line.drain() } returns Unit
            every { line.close() } returns Unit

            JvmSpeechRecognitionChimePlayer(openLine = { line }).play()

            verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
            verify(exactly = 1) { line.start() }
            verify(exactly = 1) { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) }
            verify(exactly = 1) { line.drain() }
            verify(exactly = 1) { line.close() }
            confirmVerified(line)
        }

    @Test
    fun `書き込みに失敗してもラインを閉じて例外を伝える`() =
        runTest {
            every { line.open(match { it.isChimeFormat() }) } returns Unit
            every { line.start() } returns Unit
            every { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) } throws
                IllegalStateException("write failed")
            every { line.close() } returns Unit

            val error =
                assertFailsWith<IllegalStateException> {
                    JvmSpeechRecognitionChimePlayer(openLine = { line }).play()
                }

            assertEquals("write failed", error.message)
            verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
            verify(exactly = 1) { line.start() }
            verify(exactly = 1) { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) }
            verify(exactly = 1) { line.close() }
            confirmVerified(line)
        }

    /** [AudioFormat] は equals を実装していないため、確認音の形式（44.1kHz・16ビット・モノラル・符号付き・リトルエンディアン）をフィールドで比べる。 */
    private fun AudioFormat.isChimeFormat() =
        sampleRate == WAKE_WORD_CHIME_SAMPLE_RATE.toFloat() &&
            sampleSizeInBits == 16 &&
            channels == 1 &&
            encoding == AudioFormat.Encoding.PCM_SIGNED &&
            !isBigEndian
}
