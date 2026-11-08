package kurou.kodriver.core.speechrecognitiondata.speech

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.SpeechRecognitionChime
import java.util.concurrent.CountDownLatch
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

            JvmSpeechRecognitionChimePlayer(openLine = { line }).play(SpeechRecognitionChime.WakeWordDetected)

            verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
            verify(exactly = 1) { line.start() }
            verify(exactly = 1) { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) }
            verify(exactly = 1) { line.drain() }
            verify(exactly = 1) { line.close() }
            confirmVerified(line)
        }

    @Test
    fun `時間切れの確認音は下降音の波形を書き込む`() =
        runTest {
            val timeoutBytes = commandTimeoutChimePcm().toLittleEndianBytes()
            val timeoutSize = timeoutBytes.size
            every { line.open(match { it.isChimeFormat() }) } returns Unit
            every { line.start() } returns Unit
            every { line.write(match { it.contentEquals(timeoutBytes) }, 0, timeoutSize) } returns timeoutSize
            every { line.drain() } returns Unit
            every { line.close() } returns Unit

            JvmSpeechRecognitionChimePlayer(openLine = { line }).play(SpeechRecognitionChime.CommandTimedOut)

            verify(exactly = 1) { line.write(match { it.contentEquals(timeoutBytes) }, 0, timeoutSize) }
            verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
            verify(exactly = 1) { line.start() }
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
                    JvmSpeechRecognitionChimePlayer(openLine = { line }).play(SpeechRecognitionChime.WakeWordDetected)
                }

            assertEquals("write failed", error.message)
            verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
            verify(exactly = 1) { line.start() }
            verify(exactly = 1) { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) }
            verify(exactly = 1) { line.close() }
            confirmVerified(line)
        }

    @Test
    fun `再生中に取り消されたら出力を止めて戻らせ、ラインを閉じる`() {
        val writing = CountDownLatch(1)
        val released = CountDownLatch(1)
        every { line.open(match { it.isChimeFormat() }) } returns Unit
        every { line.start() } returns Unit
        every { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) } answers {
            // 実際の出力と同じく、止められるまで write が戻らない
            writing.countDown()
            released.await()
            expectedSize
        }
        every { line.stop() } answers { released.countDown() }
        every { line.flush() } returns Unit
        every { line.drain() } returns Unit
        every { line.close() } returns Unit

        runBlocking {
            val job =
                launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
                    JvmSpeechRecognitionChimePlayer(openLine = { line }).play(SpeechRecognitionChime.WakeWordDetected)
                }
            writing.await()

            withTimeout(5_000) { job.cancelAndJoin() }
        }

        verify(exactly = 1) { line.open(match { it.isChimeFormat() }) }
        verify(exactly = 1) { line.start() }
        verify(exactly = 1) { line.write(match { it.contentEquals(expectedBytes) }, 0, expectedSize) }
        verify(exactly = 1) { line.stop() }
        verify(exactly = 1) { line.flush() }
        verify(exactly = 1) { line.drain() }
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
