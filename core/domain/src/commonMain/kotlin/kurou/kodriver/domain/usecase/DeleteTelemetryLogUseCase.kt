package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TelemetryLogRepository

public class DeleteTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    public suspend operator fun invoke(id: Long) {
        repository.deleteTelemetryLog(id)
    }
}
