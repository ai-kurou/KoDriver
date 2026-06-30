package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import org.koin.dsl.module

val fakeTelemetryLogListModule = module {
    single<TelemetryLogRepository> { FakeTelemetryLogRepository() }
}

private class FakeTelemetryLogRepository : TelemetryLogRepository {
    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = flowOf(emptyList())

    override suspend fun saveTelemetryLog(log: TelemetryLog) = Unit
}
