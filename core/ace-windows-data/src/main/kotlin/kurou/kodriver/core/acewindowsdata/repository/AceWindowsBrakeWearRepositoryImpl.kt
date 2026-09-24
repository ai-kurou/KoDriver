package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.repository.AceWindowsBrakeWearRepository

internal class AceWindowsBrakeWearRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<AceWindowsBrakeWearData> =
        source.bufferFlow.map { AceWindowsMapper.mapBrakeWear(it) }
}
