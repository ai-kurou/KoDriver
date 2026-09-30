package kurou.kodriver.core.narrator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("TooManyFunctions")
class WavDurationTest {
    @Test
    fun `fmt と data だけの WAV から再生時間を算出する`() {
        val bytes = wav(chunks = listOf(fmtChunk(), dataChunk(BYTE_RATE)))

        assertEquals(1000L, wavDurationMs(bytes))
    }

    @Test
    fun `fmt と data の間に LIST チャンクがあっても再生時間を算出する`() {
        val bytes = wav(chunks = listOf(fmtChunk(), chunk("LIST", ByteArray(16)), dataChunk(BYTE_RATE / 2)))

        assertEquals(500L, wavDurationMs(bytes))
    }

    @Test
    fun `奇数サイズのチャンクがあってもパディングを考慮して data チャンクへ到達する`() {
        val bytes =
            wav(
                chunks =
                    listOf(
                        fmtChunk(),
                        chunk("LIST", ByteArray(15)) + byteArrayOf(0),
                        dataChunk(BYTE_RATE),
                    ),
            )

        assertEquals(1000L, wavDurationMs(bytes))
    }

    @Test
    fun `fmt チャンクが data チャンクより後にある WAV では再生時間を判定できない`() {
        val bytes = wav(chunks = listOf(dataChunk(BYTE_RATE), fmtChunk()))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `負のチャンクサイズでも無限ループせず null を返す`() {
        val bytes = wav(chunks = listOf(rawChunk("LIST", size = -8, body = ByteArray(0)), dataChunk(BYTE_RATE)))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `チャンクサイズが 0 でも無限ループせず null を返す`() {
        val bytes = wav(chunks = listOf(rawChunk("LIST", size = 0, body = ByteArray(0)), dataChunk(BYTE_RATE)))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `チャンクサイズが巨大でもオーバーフローせず null を返す`() {
        val hugeChunk = rawChunk("LIST", size = Int.MAX_VALUE, body = ByteArray(0))
        val bytes = wav(chunks = listOf(hugeChunk, dataChunk(BYTE_RATE)))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `data のチャンクサイズが実バイト数を超える場合は実バイト数で再生時間を算出する`() {
        val truncatedData = rawChunk("data", size = BYTE_RATE, body = ByteArray(BYTE_RATE / 2))
        val bytes = wav(chunks = listOf(fmtChunk(), truncatedData))

        assertEquals(500L, wavDurationMs(bytes))
    }

    @Test
    fun `RIFF ヘッダが無いバイト列では null を返す`() {
        val bytes = wav(chunks = listOf(fmtChunk(), dataChunk(BYTE_RATE)), riffId = "RIFX")

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `WAVE 以外のフォーマットでは null を返す`() {
        val bytes = wav(chunks = listOf(fmtChunk(), dataChunk(BYTE_RATE)), formatId = "AVI ")

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `ヘッダ長に満たないバイト列では null を返す`() {
        assertNull(wavDurationMs(ByteArray(11)))
    }

    @Test
    fun `空のバイト列では null を返す`() {
        assertNull(wavDurationMs(ByteArray(0)))
    }

    @Test
    fun `byteRate が 0 の WAV では null を返す`() {
        val bytes = wav(chunks = listOf(fmtChunk(byteRate = 0), dataChunk(BYTE_RATE)))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `byteRate が負の WAV では null を返す`() {
        val bytes = wav(chunks = listOf(fmtChunk(byteRate = -1), dataChunk(BYTE_RATE)))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `data チャンクが無い WAV では null を返す`() {
        val bytes = wav(chunks = listOf(fmtChunk()))

        assertNull(wavDurationMs(bytes))
    }

    @Test
    fun `fmt チャンクが byteRate の位置まで届かず途中で切れている場合は null を返す`() {
        val bytes = wav(chunks = listOf(rawChunk("fmt ", size = 8, body = ByteArray(8))))

        assertNull(wavDurationMs(bytes))
    }

    private companion object {
        /** 16000Hz・モノラル・16bit 相当。1 秒分が [BYTE_RATE] バイトになるので期待値が分かりやすい。 */
        const val BYTE_RATE = 32_000

        fun wav(
            chunks: List<ByteArray>,
            riffId: String = "RIFF",
            formatId: String = "WAVE",
        ): ByteArray {
            val body = chunks.reduce { acc, bytes -> acc + bytes }
            return riffId.encodeToByteArray() + int32LE(4 + body.size) + formatId.encodeToByteArray() + body
        }

        fun fmtChunk(byteRate: Int = BYTE_RATE): ByteArray {
            val body =
                int16LE(1) + // audioFormat: PCM
                    int16LE(1) + // numChannels
                    int32LE(16_000) + // sampleRate
                    int32LE(byteRate) +
                    int16LE(2) + // blockAlign
                    int16LE(16) // bitsPerSample
            return chunk("fmt ", body)
        }

        fun dataChunk(size: Int): ByteArray = chunk("data", ByteArray(size))

        fun chunk(
            id: String,
            body: ByteArray,
        ): ByteArray = rawChunk(id, body.size, body)

        /** チャンクサイズを本体の長さと独立に指定できる版。壊れたチャンクサイズを再現するために使う。 */
        fun rawChunk(
            id: String,
            size: Int,
            body: ByteArray,
        ): ByteArray = id.encodeToByteArray() + int32LE(size) + body

        fun int32LE(value: Int): ByteArray =
            byteArrayOf(
                value.toByte(),
                (value shr 8).toByte(),
                (value shr 16).toByte(),
                (value shr 24).toByte(),
            )

        fun int16LE(value: Int): ByteArray = byteArrayOf(value.toByte(), (value shr 8).toByte())
    }
}
