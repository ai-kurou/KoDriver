package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import java.nio.ByteBuffer

internal class LmuWindowsVirtualEnergyRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVirtualEnergyRepository {

    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> =
        source.bufferFlow.mapNotNull { readVirtualEnergy(it) }

    private fun readVirtualEnergy(buffer: ByteBuffer): LmuWindowsVirtualEnergyData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        return LmuWindowsVirtualEnergyData(
            remainingRatio = LmuWindowsMapper.readVirtualEnergyRatio(buffer, vehicleBase),
        )
    }
}
