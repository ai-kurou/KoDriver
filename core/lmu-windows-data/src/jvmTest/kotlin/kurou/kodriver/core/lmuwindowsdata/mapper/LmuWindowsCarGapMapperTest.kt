package kurou.kodriver.core.lmuwindowsdata.mapper

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsCarGapMapperTest {
    @Test
    fun `readCarGapは指定車両の4種類の時間差を補正せずDoubleで返す`() {
        val base = 100
        val buffer = ByteBuffer.allocate(base + 800).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putFloat(base + 780, 1.25f)
        buffer.putFloat(base + 784, 0.0f)
        buffer.putFloat(base + 788, -1.5f)
        buffer.putFloat(base + 792, 4.5f)

        val result = LmuWindowsCarGapMapper.readCarGap(buffer, base)

        assertEquals(1.25, result.timeGapCarAheadSeconds)
        assertEquals(0.0, result.timeGapCarBehindSeconds)
        assertEquals(-1.5, result.timeGapPlaceAheadSeconds)
        assertEquals(4.5, result.timeGapPlaceBehindSeconds)
        assertEquals(null, result.overallGapAheadSeconds)
        assertEquals(null, result.overallGapBehindSeconds)
    }

    @Test
    fun `全体ギャップは異クラスを含め自車とガレージを除外して周回補正した最小差を返す`() {
        val buffer = scoringBuffer()
        vehicle(buffer, 1, 225.0, "GT")
        vehicle(buffer, 2, -190.0, "Hypercar")
        vehicle(buffer, 3, 21.0, "GT", garage = true)
        vehicle(buffer, 4, 22.0, "GT", player = true)

        val result = LmuWindowsCarGapMapper.readCarGap(buffer, 128_468, 2192)

        assertEquals(5.0, result.overallGapAheadSeconds)
        assertEquals(10.0, result.overallGapBehindSeconds)
    }

    @Test
    fun `対象車両がなければ全体ギャップはnull`() {
        val buffer = scoringBuffer()
        vehicle(buffer, 1, 21.0, "GT", garage = true)
        vehicle(buffer, 2, 22.0, "GT", player = true)

        val result = LmuWindowsCarGapMapper.readCarGap(buffer, 128_468, 2192)

        assertEquals(null, result.overallGapAheadSeconds)
        assertEquals(null, result.overallGapBehindSeconds)
    }

    @Test
    fun `推定ラップタイムが無効なら全体ギャップはnull`() {
        for (lapTime in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val buffer = scoringBuffer()
            vehicle(buffer, 1, 25.0, "GT")
            buffer.putDouble(2192 + 472, lapTime)

            val result = LmuWindowsCarGapMapper.readCarGap(buffer, 128_468, 2192)

            assertEquals(null, result.overallGapAheadSeconds)
            assertEquals(null, result.overallGapBehindSeconds)
        }
    }

    @Test
    fun `非有限の時間差は全体ギャップの対象から除外する`() {
        val buffer = scoringBuffer()
        vehicle(buffer, 1, Double.NaN, "GT")
        vehicle(buffer, 2, Double.POSITIVE_INFINITY, "GT")

        val result = LmuWindowsCarGapMapper.readCarGap(buffer, 128_468, 2192)

        assertEquals(null, result.overallGapAheadSeconds)
        assertEquals(null, result.overallGapBehindSeconds)
    }

    private fun scoringBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)
        vehicle(buffer, 0, 20.0, "Hypercar", player = true)
        buffer.putDouble(2192 + 472, 100.0)
        return buffer
    }

    private fun vehicle(
        buffer: ByteBuffer,
        index: Int,
        time: Double,
        vehicleClass: String,
        garage: Boolean = false,
        player: Boolean = false,
    ) {
        buffer.putInt(1632 + 104, index + 1)
        val base = 2192 + index * 584
        buffer.putDouble(base + 464, time)
        buffer.put(base + 196, if (player) 1.toByte() else 0.toByte())
        buffer.put(base + 507, if (garage) 1.toByte() else 0.toByte())
        vehicleClass.toByteArray(Charsets.US_ASCII).forEachIndexed { offset, byte ->
            buffer.put(base + 200 + offset, byte)
        }
    }
}
