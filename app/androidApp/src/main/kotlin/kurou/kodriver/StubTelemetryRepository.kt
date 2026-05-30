package kurou.kodriver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.repository.TelemetryRepository
import kurou.kodriver.domain.model.TelemetryData

class StubTelemetryRepository : TelemetryRepository {
    override fun telemetryStream(): Flow<TelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() {}
}
