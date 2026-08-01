package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import java.nio.ByteBuffer

internal class LmuWindowsTyreWearRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = source.bufferFlow.mapNotNull { readTyreWear(it) }

    private fun readTyreWear(buffer: ByteBuffer): LmuWindowsTyreWearData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val wheels = LmuWindowsMapper.readWearFractions(buffer, vehicleBase)
        return LmuWindowsTyreWearData(wheels)
    }
}
