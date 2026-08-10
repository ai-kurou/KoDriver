package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.repository.AceWindowsFuelRepository

internal class AceWindowsFuelRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = source.bufferFlow.map { AceWindowsMapper.mapFuel(it) }

    override suspend fun isConnected(): Boolean = source.isConnected()
}
