package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTelemetryData

interface LmuWindowsRepository {
    fun telemetryStream(): Flow<LmuWindowsTelemetryData>
    suspend fun isConnected(): Boolean
    suspend fun disconnect()
}
