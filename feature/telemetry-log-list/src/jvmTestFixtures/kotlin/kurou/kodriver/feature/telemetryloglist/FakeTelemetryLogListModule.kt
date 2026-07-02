package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
import org.koin.dsl.module

val fakeTelemetryLogListModule = module {
    single<TelemetryLogRepository> { fakeTelemetryLogRepository }
}

val fakeTelemetryLogRepository = FakeTelemetryLogRepository()

class FakeTelemetryLogRepository : TelemetryLogRepository {
    private val logs = MutableStateFlow(emptyList<TelemetryLog>())

    override fun observeTelemetryLogs() = logs

    override fun observeTelemetryLogDetail(id: Long) = logs.map { logs ->
        val current = logs.firstOrNull { it.id == id } ?: return@map null
        TelemetryLogDetail(current = current, previous = null)
    }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        emit(logs.value + log)
    }

    fun emit(value: List<TelemetryLog>) {
        logs.update { value }
    }

    fun clear() {
        emit(emptyList())
    }
}
