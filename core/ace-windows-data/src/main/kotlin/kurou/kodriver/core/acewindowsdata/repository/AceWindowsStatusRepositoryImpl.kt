package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.repository.AceWindowsStatusRepository

internal class AceWindowsStatusRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = source.bufferFlow.map { AceWindowsMapper.mapStatus(it) }
}
