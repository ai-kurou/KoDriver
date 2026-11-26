package kurou.kodriver.domain.telemetrylog.usecase

import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository

public class DeleteTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    public suspend operator fun invoke(id: Long) {
        repository.deleteTelemetryLog(id)
    }
}
