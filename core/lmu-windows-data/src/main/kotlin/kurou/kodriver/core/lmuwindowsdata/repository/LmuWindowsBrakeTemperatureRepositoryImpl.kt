package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWheelDoubleField
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperatureRepository
import java.nio.ByteBuffer

internal class LmuWindowsBrakeTemperatureRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsBrakeTemperatureRepository {
    override fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData> =
        source.bufferFlow.mapNotNull { readBrakeTemperature(it) }

    private fun readBrakeTemperature(buffer: ByteBuffer): LmuWindowsBrakeTemperatureData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val wheels =
            LmuWindowsMapper
                .readWheelDoubles(buffer, vehicleBase, LmuWheelDoubleField.BRAKE_TEMPERATURE)
                .mapValues { (_, kelvin) -> CelsiusReading((kelvin - KELVIN_OFFSET).toFloat()) }
        return LmuWindowsBrakeTemperatureData(wheels)
    }

    private companion object {
        const val KELVIN_OFFSET = 273.15
    }
}
