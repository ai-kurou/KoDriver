package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsClassCarGapMapper
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository
import java.nio.ByteBuffer

internal class LmuWindowsClassCarGapRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsClassCarGapRepository {
    override fun classCarGapStream(): Flow<LmuWindowsClassCarGapData> =
        source.bufferFlow.mapNotNull {
            readClassCarGap(it)
        }

    private fun readClassCarGap(buffer: ByteBuffer): LmuWindowsClassCarGapData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleScoringBase(buffer) ?: return null
        return LmuWindowsClassCarGapMapper.readClassCarGap(buffer, vehicleBase)
    }
}
