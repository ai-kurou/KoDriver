@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import android.media.AudioFormat
import android.media.AudioRecord
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kurou.kodriver.domain.microphone.model.MICROPHONE_SAMPLE_RATE_HZ
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class AudioRecordPcmRecorderTest {
    @AfterTest
    fun tearDown() {
        unmockkStatic(AudioRecord::class)
    }

    @Test
    fun `開始前は読み取りが-1を返し、停止と解放は何もしない`() {
        val recorder = AudioRecordPcmRecorder()

        assertEquals(-1, recorder.read(ByteArray(4)))
        recorder.interrupt()
        recorder.close()
    }

    @Test
    fun `マイクの入力形式に対応していないと開始に失敗する`() {
        mockkStatic(AudioRecord::class)
        every {
            AudioRecord.getMinBufferSize(
                MICROPHONE_SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
        } returns AudioRecord.ERROR_BAD_VALUE

        val error = assertFailsWith<IllegalStateException> { AudioRecordPcmRecorder().start() }

        assertEquals("マイクの入力形式に対応していません", error.message)
    }

    @Test
    fun `開始・読み取り・停止・解放ができる`() {
        val recorder = AudioRecordPcmRecorder()
        runCatching { recorder.start() }
        val buffer = ByteArray(8)

        recorder.read(buffer)
        recorder.interrupt()
        recorder.close()

        assertEquals(-1, recorder.read(buffer))
    }

    @Test
    fun `停止が状態不正で失敗しても握りつぶす`() {
        val record = mockk<AudioRecord>()
        every { record.stop() } throws IllegalStateException("released")
        val recorder = AudioRecordPcmRecorder()
        AudioRecordPcmRecorder::class.java
            .getDeclaredField("record")
            .apply { isAccessible = true }
            .set(recorder, record)

        recorder.interrupt()

        verify(exactly = 1) { record.stop() }
        confirmVerified(record)
    }
}
