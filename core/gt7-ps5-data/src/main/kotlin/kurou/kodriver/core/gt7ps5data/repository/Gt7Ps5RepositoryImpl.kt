package kurou.kodriver.core.gt7ps5data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.mapper.Gt7Ps5Mapper
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository

internal class Gt7Ps5RepositoryImpl(
    private val source: Gt7Ps5PacketSource,
) : Gt7Ps5Repository {

    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> =
        source.packetFlow.map { Gt7Ps5Mapper.map(it) }
}
