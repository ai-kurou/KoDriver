package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.core.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import java.nio.ByteBuffer

internal class LmuWindowsVehicleDamageRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> =
        source.bufferFlow.mapNotNull { readDamage(it) }

    private fun readDamage(buffer: ByteBuffer): LmuWindowsVehicleDamageData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        return LmuWindowsVehicleDamageData(
            overheating = buffer.get(vehicleBase + OFF_OVERHEATING).toInt() != 0,
            partDetached = buffer.get(vehicleBase + OFF_PART_DETACHED).toInt() != 0,
            lastImpactMagnitude = buffer.getDouble(vehicleBase + OFF_LAST_IMPACT_MAGNITUDE),
        )
    }

    companion object {
        private const val OFF_OVERHEATING = 541
        private const val OFF_PART_DETACHED = 542
        private const val OFF_LAST_IMPACT_MAGNITUDE = 560
    }
}
