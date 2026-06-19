package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.VehicleDamageRepository
import java.nio.ByteBuffer

internal class SharedMemoryVehicleDamageRepository(
    private val source: SharedLmuWindowsMemorySource,
) : VehicleDamageRepository {

    override fun vehicleDamageStream(): Flow<VehicleDamageData> =
        source.bufferFlow.mapNotNull { readDamage(it) }

    private fun readDamage(buffer: ByteBuffer): VehicleDamageData? {
        val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
        val activeVehicles = buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF
        if (activeVehicles == 0 || playerIdx >= activeVehicles) return null

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
        return VehicleDamageData(
            overheating = buffer.get(vehicleBase + OFF_OVERHEATING).toInt() != 0,
            partDetached = buffer.get(vehicleBase + OFF_PART_DETACHED).toInt() != 0,
            lastImpactMagnitude = buffer.getDouble(vehicleBase + OFF_LAST_IMPACT_MAGNITUDE),
        )
    }

    companion object {
        private const val TELEMETRY_BASE = 128_464

        private const val OFF_ACTIVE_VEHICLES = 0
        private const val OFF_PLAYER_VEHICLE_IDX = 1
        private const val OFF_TELEM_INFO = 4
        private const val VEHICLE_STRIDE = 1_888

        private const val OFF_OVERHEATING = 541
        private const val OFF_PART_DETACHED = 542
        private const val OFF_LAST_IMPACT_MAGNITUDE = 560
    }
}
