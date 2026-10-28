package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository

internal class AceWindowsGapRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsGapRepository {
    override fun gapStream(): Flow<AceWindowsGapData> = source.bufferFlow.map { AceWindowsMapper.mapGap(it) }
}
