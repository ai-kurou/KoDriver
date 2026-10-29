package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapMapperTest {
    @Test
    fun `同クラスの車両だけを対象とし別クラスとガレージ内と他のプレイヤーを除外する`() {
        val buffer = buffer()
        vehicle(buffer, 1, 25.0)
        vehicle(buffer, 2, 21.0, "GT")
        vehicle(buffer, 3, 22.0, garage = true)
        vehicle(buffer, 4, 23.0, player = true)

        assertEquals(
            LmuWindowsClassCarGapData(5.0, 95.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `対象車両がなければ両方null`() {
        val buffer = buffer()

        assertEquals(
            LmuWindowsClassCarGapData(null, null),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `複数車両から前後それぞれの最小値を選ぶ`() {
        val buffer = buffer()
        vehicle(buffer, 1, 25.0)
        vehicle(buffer, 2, 110.0)
        vehicle(buffer, 3, 60.0)

        assertEquals(
            LmuWindowsClassCarGapData(5.0, 10.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `前方の周回補正を行う`() {
        val buffer = buffer()
        vehicle(buffer, 1, 225.0)

        assertEquals(
            LmuWindowsClassCarGapData(5.0, 95.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `後方の周回補正を行う`() {
        val buffer = buffer()
        vehicle(buffer, 1, -190.0)

        assertEquals(
            LmuWindowsClassCarGapData(90.0, 10.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `同位置なら前後ともゼロ`() {
        val buffer = buffer()
        vehicle(buffer, 1, 120.0)

        assertEquals(
            LmuWindowsClassCarGapData(0.0, 0.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `非有限の他車時間と差分を除外する`() {
        val buffer = buffer()
        vehicle(buffer, 1, Double.NaN)
        vehicle(buffer, 2, Double.POSITIVE_INFINITY)
        vehicle(buffer, 3, Double.NEGATIVE_INFINITY)

        assertEquals(
            LmuWindowsClassCarGapData(null, null),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `推定ラップタイムが非有限またはゼロ以下なら両方null`() {
        for (lapTime in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            val buffer = buffer()
            buffer.putDouble(2192 + 472, lapTime)
            vehicle(buffer, 1, 25.0)

            assertEquals(
                LmuWindowsClassCarGapData(null, null),
                LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
            )
        }
    }

    @Test
    fun `自車時間が非有限なら対象を除外する`() {
        val buffer = buffer()
        buffer.putDouble(2192 + 464, Double.NaN)
        vehicle(buffer, 1, 25.0)

        assertEquals(
            LmuWindowsClassCarGapData(null, null),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `台数はゼロから104に制限する`() {
        val buffer = buffer()
        vehicle(buffer, 103, 25.0)
        buffer.putInt(1632 + 104, Int.MAX_VALUE)
        assertEquals(
            LmuWindowsClassCarGapData(5.0, 95.0),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
        buffer.putInt(1632 + 104, -1)
        assertEquals(
            LmuWindowsClassCarGapData(null, null),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `総合順位からクラス内のトップと中間と最後尾を求める`() {
        for ((place, position) in listOf(1 to 1, 3 to 2, 255 to 3)) {
            val buffer = buffer()
            buffer.put(2192 + 199, place.toByte())
            vehicle(buffer, 1, 25.0, place = 2)
            vehicle(buffer, 2, 110.0, place = 200)

            assertEquals(
                LmuWindowsClassCarGapData(5.0, 10.0, classPosition = position, classVehicleCount = 3),
                LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
            )
        }
    }

    @Test
    fun `順位と台数は別クラスとガレージ内の他車を数えずガレージ内の自車を含む`() {
        val buffer = buffer()
        buffer.put(2192 + 199, 5)
        buffer.put(2192 + 507, 1)
        vehicle(buffer, 1, 25.0, place = 3)
        vehicle(buffer, 2, 21.0, "GT", place = 1)
        vehicle(buffer, 3, 22.0, garage = true, place = 2)
        vehicle(buffer, 4, 110.0, place = 6)

        assertEquals(
            LmuWindowsClassCarGapData(5.0, 10.0, classPosition = 2, classVehicleCount = 3),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `自車の総合順位が未設定なら順位と台数はnullでギャップは維持する`() {
        val buffer = buffer()
        vehicle(buffer, 1, 25.0, place = 1)

        assertEquals(
            LmuWindowsClassCarGapData(5.0, 95.0, classPosition = null, classVehicleCount = null),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `ギャップを算出できなくても同クラスが自車のみなら順位と台数は1`() {
        val buffer = buffer()
        buffer.put(2192 + 199, 1)
        buffer.putDouble(2192 + 472, 0.0)

        assertEquals(
            LmuWindowsClassCarGapData(null, null, classPosition = 1, classVehicleCount = 1),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    @Test
    fun `順位の走査台数もゼロから104に制限する`() {
        val buffer = buffer()
        buffer.put(2192 + 199, 2)
        vehicle(buffer, 103, 25.0, place = 1)
        buffer.putInt(1632 + 104, Int.MAX_VALUE)
        assertEquals(
            LmuWindowsClassCarGapData(5.0, 95.0, classPosition = 2, classVehicleCount = 2),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
        buffer.putInt(1632 + 104, -1)
        assertEquals(
            LmuWindowsClassCarGapData(null, null, classPosition = 1, classVehicleCount = 1),
            LmuWindowsClassCarGapMapper.readClassCarGap(buffer, 2192),
        )
    }

    private fun buffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(2192 + 584 * 104).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1632 + 104, 5)
        vehicle(buffer, 0, 20.0, player = true)
        buffer.putDouble(2192 + 472, 100.0)
        return buffer
    }

    private fun vehicle(
        buffer: ByteBuffer,
        index: Int,
        time: Double,
        vehicleClass: String = "Hypercar",
        garage: Boolean = false,
        player: Boolean = false,
        place: Int = 0,
    ) {
        val base = 2192 + index * 584
        buffer.putDouble(base + 464, time)
        buffer.put(base + 199, place.toByte())
        buffer.put(base + 196, if (player) 1.toByte() else 0.toByte())
        buffer.put(base + 507, if (garage) 1.toByte() else 0.toByte())
        vehicleClass.toByteArray(Charsets.US_ASCII).forEachIndexed { offset, byte ->
            buffer.put(base + 200 + offset, byte)
        }
    }
}
