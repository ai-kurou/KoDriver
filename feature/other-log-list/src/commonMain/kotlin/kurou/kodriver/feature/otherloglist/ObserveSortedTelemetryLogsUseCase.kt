package kurou.kodriver.feature.otherloglist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog
import kurou.kodriver.domain.telemetrylog.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class ObserveSortedTelemetryLogsUseCase(
    private val observeTelemetryLogs: ObserveTelemetryLogsUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) {
    operator fun invoke(): Flow<List<TelemetryLog>> =
        combine(observeTelemetryLogs(), observeSelectedSimulator()) { logs, selected ->
            logs
                .filter { it.simulator == selected }
                .sortedWith(
                    compareByDescending<TelemetryLog> { it.createdAt }
                        .thenByDescending { it.id },
                )
        }
}
