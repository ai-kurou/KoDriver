package kurou.kodriver.core.acewindowsdata.mapper

import kurou.kodriver.domain.model.AceWindowsFlagType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsMapperTest {

    private companion object {
        const val OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT = 200
        const val OFF_FLAG = 2404
        const val BUFFER_SIZE = 8_192
    }

    private fun buffer(fuelPercent: Float): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also {
            it.putFloat(OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT, fuelPercent)
        }

    private fun flagBuffer(flagRawValue: Int): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also {
            it.putInt(OFF_FLAG, flagRawValue)
        }

    @Test
    fun `fuel_liter_current_quantity_percent を remainingPercent として取得する`() {
        val result = AceWindowsMapper.map(buffer(0.75f))

        assertEquals(0.75, result.remainingPercent, 0.0001)
    }

    @Test
    fun `残燃料0のとき remainingPercent は0を返す`() {
        val result = AceWindowsMapper.map(buffer(0.0f))

        assertEquals(0.0, result.remainingPercent, 0.0001)
    }

    @Test
    fun `flag が4のとき BLUE_FLAG として取得する`() {
        val result = AceWindowsMapper.mapFlag(flagBuffer(4))

        assertEquals(AceWindowsFlagType.BLUE_FLAG, result.flag)
    }

    @Test
    fun `flag が未知の値のとき UNKNOWN を返す`() {
        val result = AceWindowsMapper.mapFlag(flagBuffer(-1))

        assertEquals(AceWindowsFlagType.UNKNOWN, result.flag)
    }
}
