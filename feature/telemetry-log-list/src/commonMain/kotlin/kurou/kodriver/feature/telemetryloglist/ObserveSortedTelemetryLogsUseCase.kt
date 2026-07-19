package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase

class ObserveSortedTelemetryLogsUseCase(
    private val observeTelemetryLogs: ObserveTelemetryLogsUseCase,
) {
    operator fun invoke(): Flow<List<TelemetryLog>> =
        observeTelemetryLogs().map { logs ->
            logs.sortedWith(
                compareByDescending<TelemetryLog> { it.createdAt }
                    .thenByDescending { it.id },
            )
        }
}
