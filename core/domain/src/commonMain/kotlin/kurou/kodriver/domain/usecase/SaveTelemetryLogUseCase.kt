package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository

class SaveTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    suspend operator fun invoke(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        telemetryJson: String,
    ) {
        repository.saveTelemetryLog(
            createdAt = createdAt,
            simulator = simulator,
            readoutItemKey = readoutItemKey,
            telemetryJson = telemetryJson,
        )
    }
}
