package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.repository.AceWindowsFlagRepository

internal class AceWindowsFlagRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsFlagRepository {

    override fun flagStream(): Flow<AceWindowsFlagData> =
        source.bufferFlow.map { AceWindowsMapper.mapFlag(it) }
}
