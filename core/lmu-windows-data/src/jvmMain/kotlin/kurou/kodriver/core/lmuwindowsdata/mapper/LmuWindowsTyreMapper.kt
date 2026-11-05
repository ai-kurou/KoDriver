package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer

/**
 * LMU 共有メモリの LMUWheel 構造体から [LmuWindowsTyreData] を組み立てる。
 * オフセットの基準値は [LmuWindowsMapper] を参照する。
 */
internal object LmuWindowsTyreMapper {
    private const val OFF_WHEEL_BRAKE_TEMP = 24
    private const val OFF_WHEEL_PRESSURE = 120
    private const val OFF_WHEEL_TEMPERATURE = 128
    private const val OFF_WHEEL_WEAR = 152
    private const val OFF_WHEEL_TIRE_CARCASS_TEMPERATURE = 204
    private const val KELVIN_OFFSET = 273.15

    fun map(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): LmuWindowsTyreData {
        val wheels =
            WheelIndex.entries.associateWith { wheel ->
                val offset =
                    vehicleBase + LmuWindowsMapper.OFF_WHEELS + (wheel.ordinal * LmuWindowsMapper.WHEEL_STRIDE)
                val surfaceTempK = buffer.getDouble(offset + OFF_WHEEL_TEMPERATURE + 8)
                val carcassTempK = buffer.getDouble(offset + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE)
                LmuWindowsTyreWheelData(
                    surfaceTemperature = CelsiusReading((surfaceTempK - KELVIN_OFFSET).toFloat()),
                    carcassTemperature = CelsiusReading((carcassTempK - KELVIN_OFFSET).toFloat()),
                    brakeTemperature =
                        CelsiusReading(
                            (buffer.getDouble(offset + OFF_WHEEL_BRAKE_TEMP) - KELVIN_OFFSET).toFloat(),
                        ),
                    pressureKpa = PressureKpa(buffer.getDouble(offset + OFF_WHEEL_PRESSURE)),
                    wear = LmuWindowsTyreWearRatio(buffer.getDouble(offset + OFF_WHEEL_WEAR)),
                )
            }
        return LmuWindowsTyreData(wheels)
    }
}
