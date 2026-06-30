package kurou.kodriver.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.datasource.TelemetryLogDao
import kurou.kodriver.data.model.toDomain
import kurou.kodriver.data.model.toEntity
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class TelemetryLogRepositoryImpl(
    private val dao: TelemetryLogDao,
) : TelemetryLogRepository {
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> =
        dao.observeTelemetryLogs().map { logs -> logs.map { it.toDomain() } }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?> =
        dao.observeTelemetryLog(id).flatMapLatest { current ->
            if (current == null) {
                flowOf(null)
            } else {
                dao.observePreviousTelemetryLog(
                    createdAt = current.createdAt,
                    id = current.id,
                ).map { previous ->
                    TelemetryLogDetail(
                        current = current.toDomain(),
                        previous = previous?.toDomain(),
                    )
                }
            }
        }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        dao.insert(log.toEntity())
    }
}
