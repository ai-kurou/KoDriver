package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.data.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.repository.LmuWindowsRepository

internal class LmuWindowsRepositoryImpl(
    private val source: SharedLmuWindowsMemorySource,
) : LmuWindowsRepository {

    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> =
        source.bufferFlow.map { LmuWindowsMapper.map(it) }

    override suspend fun isConnected(): Boolean = source.isConnected()

    override suspend fun disconnect() = source.disconnect()
}
