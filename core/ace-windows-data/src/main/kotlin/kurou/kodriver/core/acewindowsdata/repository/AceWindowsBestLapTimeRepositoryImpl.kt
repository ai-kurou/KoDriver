package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository

internal class AceWindowsBestLapTimeRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsBestLapTimeRepository {
    override fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData> =
        source.bufferFlow.map { AceWindowsMapper.mapBestLapTime(it) }
}
