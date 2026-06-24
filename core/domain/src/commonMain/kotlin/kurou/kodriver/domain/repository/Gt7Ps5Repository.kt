package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData

interface Gt7Ps5Repository {
    fun telemetryStream(): Flow<Gt7Ps5TelemetryData>
    suspend fun isConnected(): Boolean
}
