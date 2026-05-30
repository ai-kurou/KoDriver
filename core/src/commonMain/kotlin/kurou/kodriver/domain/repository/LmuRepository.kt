package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuTelemetryData

interface LmuRepository {
    fun telemetryStream(): Flow<LmuTelemetryData>
    suspend fun isConnected(): Boolean
    suspend fun disconnect()
}
