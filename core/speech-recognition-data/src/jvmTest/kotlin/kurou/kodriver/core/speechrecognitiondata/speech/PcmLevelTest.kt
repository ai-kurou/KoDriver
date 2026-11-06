package kurou.kodriver.core.speechrecognitiondata.speech

import kotlin.test.Test
import kotlin.test.assertEquals

class PcmLevelTest {
    private fun pcm(vararg samples: Int): ByteArray =
        ByteArray(samples.size * 2).also { bytes ->
            samples.forEachIndexed { i, s ->
                bytes[i * 2] = (s and 0xFF).toByte()
                bytes[i * 2 + 1] = (s shr 8).toByte()
            }
        }

    @Test
    fun `空の断片と1サンプルに満たない断片は音量0になる`() {
        assertEquals(0f, ByteArray(0).pcmLevel())
        assertEquals(0f, byteArrayOf(1).pcmLevel())
    }

    @Test
    fun `全ゼロの断片は音量0になる`() {
        assertEquals(0f, pcm(0, 0, 0).pcmLevel())
    }

    @Test
    fun `最大振幅の断片は音量1になる`() {
        assertEquals(1f, pcm(-32768, -32768).pcmLevel())
    }

    @Test
    fun `下限より小さい断片は音量0になる`() {
        // 32 / 32768 は約 -60 dBFS。1 は下限を下回る
        assertEquals(0f, pcm(1, -1).pcmLevel())
    }

    @Test
    fun `符号に関わらず同じ音量になり下位バイトも読む`() {
        // 16384 / 32768 = 0.5 → 約 -6 dBFS は上限を超えるため1
        assertEquals(1f, pcm(16384, -16384).pcmLevel())
        // 1000 / 32768 → 約 -30.3 dBFS → (-30.3 + 60) / 50
        assertEquals(0.594f, pcm(1000, -1000).pcmLevel(), absoluteTolerance = 0.001f)
    }

    @Test
    fun `振幅の異なるサンプルは二乗平均平方根で計算する`() {
        // RMS は 32767 / sqrt(2) ≒ 23169 → 約 -3 dBFS（平均絶対値なら約 -6 dBFS）。どちらも上限を超えて1
        assertEquals(1f, pcm(0, 32767).pcmLevel())
        // RMS は 2000 / sqrt(2) ≒ 1414 → 約 -27.3 dBFS
        assertEquals(0.654f, pcm(0, 2000).pcmLevel(), absoluteTolerance = 0.001f)
    }

    @Test
    fun `端数のバイトは無視する`() {
        assertEquals(pcm(1000, -1000).pcmLevel(), (pcm(1000, -1000) + byteArrayOf(0x7F)).pcmLevel())
    }
}
