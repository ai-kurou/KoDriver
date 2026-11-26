package kurou.kodriver.domain.telemetrylog.usecase

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository

public class ResetTelemetryLogDatabaseUseCase(
    private val repository: TelemetryLogRepository,
) {
    public suspend operator fun invoke() {
        Simulator.entries.forEach { simulator ->
            repository.deleteTelemetryLogsBySimulator(simulator)
        }
    }
}
