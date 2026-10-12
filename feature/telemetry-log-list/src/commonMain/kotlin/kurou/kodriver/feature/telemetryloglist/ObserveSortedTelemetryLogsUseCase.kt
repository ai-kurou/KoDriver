package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase

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
