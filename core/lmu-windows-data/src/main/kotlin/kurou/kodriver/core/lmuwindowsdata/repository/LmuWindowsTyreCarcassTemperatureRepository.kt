package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository
import java.nio.ByteBuffer

internal class LmuWindowsTyreCarcassTemperatureRepository(
    private val source: LmuWindowsSharedMemorySource,
) : TyreCarcassTemperatureRepository {

    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> =
        source.bufferFlow.mapNotNull { readTyreCarcassTemperature(it) }

    private fun readTyreCarcassTemperature(buffer: ByteBuffer): TyreCarcassTemperatureData? {
        val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
        val activeVehicles = buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF
        if (activeVehicles == 0 || playerIdx >= activeVehicles) return null

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
        val wheels = WheelIndex.entries.associateWith { wheel ->
            val wheelBase = vehicleBase + OFF_WHEELS + wheel.ordinal * WHEEL_STRIDE
            buffer.getDouble(wheelBase + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE) - KELVIN_OFFSET
        }
        val surfaceWheels = WheelIndex.entries.associateWith { wheel ->
            val wheelBase = vehicleBase + OFF_WHEELS + wheel.ordinal * WHEEL_STRIDE
            buffer.getDouble(wheelBase + OFF_WHEEL_TIRE_SURFACE_TEMPERATURE) - KELVIN_OFFSET
        }
        return TyreCarcassTemperatureData(wheels = wheels, surfaceWheels = surfaceWheels)
    }

    companion object {
        private const val TELEMETRY_BASE = 128_464

        private const val OFF_ACTIVE_VEHICLES = 0
        private const val OFF_PLAYER_VEHICLE_IDX = 1
        private const val OFF_TELEM_INFO = 4
        private const val VEHICLE_STRIDE = 1_888

        private const val OFF_WHEELS = 848
        private const val WHEEL_STRIDE = 260
        private const val OFF_WHEEL_TIRE_CARCASS_TEMPERATURE = 204
        private const val OFF_WHEEL_TIRE_SURFACE_TEMPERATURE = 136
        private const val KELVIN_OFFSET = 273.15
    }
}
