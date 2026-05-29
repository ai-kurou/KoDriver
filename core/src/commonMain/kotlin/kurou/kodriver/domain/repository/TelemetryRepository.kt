package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryData

interface TelemetryRepository {
    fun telemetryStream(): Flow<TelemetryData>
    suspend fun isConnected(): Boolean
    suspend fun disconnect()
}
