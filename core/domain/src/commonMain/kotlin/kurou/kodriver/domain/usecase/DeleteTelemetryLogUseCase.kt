package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TelemetryLogRepository

class DeleteTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTelemetryLog(id)
    }
}
