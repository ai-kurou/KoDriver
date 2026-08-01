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
    fun `flag は全てのACEVO_FLAG_TYPEの値を取得できる`() {
        val expected =
            mapOf(
                0 to AceWindowsFlagType.NO_FLAG,
                1 to AceWindowsFlagType.WHITE_FLAG,
                2 to AceWindowsFlagType.GREEN_FLAG,
                3 to AceWindowsFlagType.RED_FLAG,
                4 to AceWindowsFlagType.BLUE_FLAG,
                5 to AceWindowsFlagType.YELLOW_FLAG,
                6 to AceWindowsFlagType.BLACK_FLAG,
                7 to AceWindowsFlagType.BLACK_WHITE_FLAG,
                8 to AceWindowsFlagType.CHECKERED_FLAG,
                9 to AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                10 to AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
            )

        expected.forEach { (rawValue, flagType) ->
            val result = AceWindowsMapper.mapFlag(flagBuffer(rawValue))

            assertEquals(flagType, result.flag, "rawValue=$rawValue")
        }
    }

    @Test
    fun `flag が未知の値のとき UNKNOWN を返す`() {
        val result = AceWindowsMapper.mapFlag(flagBuffer(-1))

        assertEquals(AceWindowsFlagType.UNKNOWN, result.flag)
    }
}
