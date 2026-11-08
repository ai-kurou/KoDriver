package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.SpeechRecognitionChime
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WakeWordChimeWaveformTest {
    private val pcm = wakeWordChimePcm()

    @Test
    fun `長さは再生時間とサンプリング周波数に一致する`() {
        assertEquals(WAKE_WORD_CHIME_SAMPLE_RATE * WAKE_WORD_CHIME_DURATION_MS / 1_000, pcm.size)
    }

    @Test
    fun `音量は満振幅の半分以下で、無音ではない`() {
        val peak = pcm.maxOf { abs(it.toInt()) }

        assertTrue(peak > 0)
        assertTrue(peak <= Short.MAX_VALUE / 2)
    }

    @Test
    fun `各音の頭と終わりは振幅が絞られていてクリックノイズが出ない`() {
        val firstToneSamples = WAKE_WORD_CHIME_SAMPLE_RATE * 90 / 1_000

        assertEquals(0, pcm.first().toInt())
        assertEquals(0, pcm.last().toInt())
        assertTrue(abs(pcm[firstToneSamples - 1].toInt()) < 200)
        assertTrue(abs(pcm[firstToneSamples].toInt()) < 200)
    }

    @Test
    fun `時間切れの音は長さが再生時間に一致し、無音ではなく音量も抑えられている`() {
        val timeoutPcm = commandTimeoutChimePcm()
        val peak = timeoutPcm.maxOf { abs(it.toInt()) }

        assertEquals(WAKE_WORD_CHIME_SAMPLE_RATE * COMMAND_TIMEOUT_CHIME_DURATION_MS / 1_000, timeoutPcm.size)
        assertTrue(peak > 0)
        assertTrue(peak <= Short.MAX_VALUE / 2)
        assertEquals(0, timeoutPcm.first().toInt())
        assertEquals(0, timeoutPcm.last().toInt())
    }

    @Test
    fun `時間切れの音は反応音と逆の下降で、同じ区間の零交差数が少ない`() {
        // 周波数が低いほど零交差が少ない。反応音は880→1318.5Hzの上昇、時間切れの音は660→440Hzの下降
        fun zeroCrossings(samples: ShortArray) = samples.toList().zipWithNext().count { (a, b) -> (a < 0) != (b < 0) }
        val head = WAKE_WORD_CHIME_SAMPLE_RATE * 90 / 1_000
        val wake = wakeWordChimePcm()
        val timeout = commandTimeoutChimePcm()

        val wakeRise = zeroCrossings(wake.copyOfRange(head, wake.size)) / (wake.size - head).toDouble()
        val wakeStart = zeroCrossings(wake.copyOfRange(0, head)) / head.toDouble()
        val timeoutStart = zeroCrossings(timeout.copyOfRange(0, head)) / head.toDouble()
        val timeoutEnd = zeroCrossings(timeout.copyOfRange(head, timeout.size)) / (timeout.size - head).toDouble()

        assertTrue(wakeRise > wakeStart)
        assertTrue(timeoutEnd < timeoutStart)
    }

    @Test
    fun `確認音の種類ごとに波形と再生時間を選ぶ`() {
        assertEquals(wakeWordChimePcm().toList(), SpeechRecognitionChime.WakeWordDetected.pcm().toList())
        assertEquals(WAKE_WORD_CHIME_DURATION_MS, SpeechRecognitionChime.WakeWordDetected.durationMs())
        assertEquals(commandTimeoutChimePcm().toList(), SpeechRecognitionChime.CommandTimedOut.pcm().toList())
        assertEquals(COMMAND_TIMEOUT_CHIME_DURATION_MS, SpeechRecognitionChime.CommandTimedOut.durationMs())
    }

    @Test
    fun `リトルエンディアンの16ビットとして2倍の長さのバイト列にできる`() {
        val bytes = shortArrayOf(0x0102, -2).toLittleEndianBytes()

        assertEquals(listOf<Byte>(0x02, 0x01, 0xFE.toByte(), 0xFF.toByte()), bytes.toList())
    }
}
