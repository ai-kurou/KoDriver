package kurou.kodriver.core.speechrecognitiondata.speech

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
    fun `リトルエンディアンの16ビットとして2倍の長さのバイト列にできる`() {
        val bytes = shortArrayOf(0x0102, -2).toLittleEndianBytes()

        assertEquals(listOf<Byte>(0x02, 0x01, 0xFE.toByte(), 0xFF.toByte()), bytes.toList())
    }
}
