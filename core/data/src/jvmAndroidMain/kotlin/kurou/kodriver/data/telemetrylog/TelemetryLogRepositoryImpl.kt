package kurou.kodriver.data.telemetrylog

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class TelemetryLogRepositoryImpl(
    private val dao: TelemetryLogDao,
) : TelemetryLogRepository {
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> =
        dao.observeTelemetryLogs().map { logs -> logs.mapNotNull { it.toDomain() } }

    override fun observeLatestNarratedTelemetryLog(): Flow<TelemetryLog?> =
        dao.observeLatestNarratedTelemetryLog(NarrationOutcome.SKIPPED.id).map { it?.toDomain() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?> =
        dao.observeTelemetryLog(id).flatMapLatest { current ->
            if (current == null) {
                flowOf(null)
            } else {
                dao
                    .observePreviousTelemetryLog(
                        createdAt = current.createdAt,
                        id = current.id,
                    ).map { previous ->
                        current.toDomain()?.let { currentLog ->
                            TelemetryLogDetail(
                                current = currentLog,
                                previous = previous?.toDomain(),
                            )
                        }
                    }
            }
        }

    override suspend fun saveTelemetryLog(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        telemetryJson: String,
    ) {
        dao.insert(
            TelemetryLogEntity(
                createdAt = createdAt,
                simulatorId = simulator.id,
                readoutItemKey = readoutItemKey.value,
                narratedText = narratedText,
                narrationOutcome = narrationOutcome.id,
                telemetryJson = telemetryJson,
            ),
        )
    }

    override suspend fun deleteAllTelemetryLogs() {
        dao.deleteAll()
    }

    override suspend fun deleteTelemetryLog(id: Long) {
        dao.delete(id)
    }
}
