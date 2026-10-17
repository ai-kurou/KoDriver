package kurou.kodriver.core.narrator

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JavaSoundAudioBackendTest {
    private val backend = JavaSoundAudioBackend()
    private val format = AudioFormat(8000f, 16, 1, true, false)
    private val line: SourceDataLine = mockk()

    @Test
    fun `指定されたフォーマットを渡し取得されたデバイスをそのまま返す`() {
        mockkStatic(AudioSystem::class)
        try {
            every { AudioSystem.getSourceDataLine(format) } returns line

            assertSame(line, backend.getLine(format))

            verify(exactly = 1) { AudioSystem.getSourceDataLine(format) }
            confirmVerified(AudioSystem::class)
        } finally {
            unmockkStatic(AudioSystem::class)
        }
    }

    @Test
    fun `デバイス取得が失敗した場合は同じ例外を呼び出し元へ伝える`() {
        val failure = LineUnavailableException("audio device unavailable")
        mockkStatic(AudioSystem::class)
        try {
            every { AudioSystem.getSourceDataLine(format) } throws failure

            assertSame(failure, assertFailsWith<LineUnavailableException> { backend.getLine(format) })

            verify(exactly = 1) { AudioSystem.getSourceDataLine(format) }
            confirmVerified(AudioSystem::class)
        } finally {
            unmockkStatic(AudioSystem::class)
        }
    }

    @Test
    fun `WAVバイト列からフォーマットと音声データを読み取る`() {
        val samples = byteArrayOf(1, 2, 3, 4, 5, 6)
        val output = ByteArrayOutputStream()
        AudioInputStream(ByteArrayInputStream(samples), format, 3).use { stream ->
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, output)
        }

        backend.openStream(output.toByteArray()).use { stream ->
            assertEquals(AudioFormat.Encoding.PCM_SIGNED, stream.format.encoding)
            assertEquals(8000f, stream.format.sampleRate)
            assertEquals(16, stream.format.sampleSizeInBits)
            assertEquals(1, stream.format.channels)
            assertEquals(2, stream.format.frameSize)
            assertEquals(3L, stream.frameLength)
            assertContentEquals(samples, stream.readAllBytes())
        }
    }

    @Test
    fun `不正な音声バイト列は解析例外を呼び出し元へ伝える`() {
        assertFailsWith<UnsupportedAudioFileException> { backend.openStream(byteArrayOf(1, 2, 3)) }
    }
}
