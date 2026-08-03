package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import java.nio.ByteBuffer

internal class LmuWindowsVehicleClassRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> =
        source.bufferFlow.mapNotNull { readVehicleClass(it) }

    private fun readVehicleClass(buffer: ByteBuffer): LmuWindowsVehicleClassData? {
        val vehicleScoringBase = LmuWindowsMapper.findPlayerVehicleScoringBase(buffer) ?: return null
        val name = LmuWindowsMapper.readVehicleClassName(buffer, vehicleScoringBase)
        return LmuWindowsVehicleClassData(name)
    }
}
