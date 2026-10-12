package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository

class ResetTelemetryLogDatabaseUseCase(
    private val repository: TelemetryLogRepository,
) {
    suspend operator fun invoke(simulator: Simulator) {
        repository.deleteTelemetryLogsBySimulator(simulator)
    }
}
