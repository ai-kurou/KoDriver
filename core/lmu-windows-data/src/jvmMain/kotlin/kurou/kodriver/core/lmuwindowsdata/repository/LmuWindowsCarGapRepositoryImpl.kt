package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsCarGapMapper
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import java.nio.ByteBuffer

internal class LmuWindowsCarGapRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsCarGapRepository {
    override fun carGapStream(): Flow<LmuWindowsCarGapData> =
        source.bufferFlow.mapNotNull {
            readCarGap(it)
        }

    private fun readCarGap(buffer: ByteBuffer): LmuWindowsCarGapData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val scoringBase = LmuWindowsMapper.findPlayerVehicleScoringBase(buffer)
        return LmuWindowsCarGapMapper.readCarGap(buffer, vehicleBase, scoringBase)
    }
}
