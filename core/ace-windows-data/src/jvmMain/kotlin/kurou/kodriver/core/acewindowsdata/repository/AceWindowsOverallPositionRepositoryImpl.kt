package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.acewindows.repository.AceWindowsOverallPositionRepository

internal class AceWindowsOverallPositionRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsOverallPositionRepository {
    override fun overallPositionStream(): Flow<AceWindowsOverallPositionData> =
        source.bufferFlow.map { AceWindowsMapper.mapOverallPosition(it) }
}
