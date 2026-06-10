package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.datasource.SharedLmuMemorySource
import kurou.kodriver.data.mapper.LmuMapper
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.repository.LmuRepository

internal class LmuRepositoryImpl(
    private val source: SharedLmuMemorySource,
) : LmuRepository {

    override fun telemetryStream(): Flow<LmuTelemetryData> =
        source.bufferFlow.map { LmuMapper.map(it) }

    override suspend fun isConnected(): Boolean = source.isConnected()

    override suspend fun disconnect() = source.disconnect()
}
