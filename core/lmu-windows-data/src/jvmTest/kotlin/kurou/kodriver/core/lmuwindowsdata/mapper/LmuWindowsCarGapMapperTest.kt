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
    }
}
