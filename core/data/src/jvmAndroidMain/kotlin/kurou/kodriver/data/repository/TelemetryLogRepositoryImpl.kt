package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.datasource.TelemetryLogDao
import kurou.kodriver.data.model.toDomain
import kurou.kodriver.data.model.toEntity
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class TelemetryLogRepositoryImpl(
    private val dao: TelemetryLogDao,
) : TelemetryLogRepository {
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> =
        dao.observeTelemetryLogs().map { logs -> logs.map { it.toDomain() } }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        dao.insert(log.toEntity())
    }
}
