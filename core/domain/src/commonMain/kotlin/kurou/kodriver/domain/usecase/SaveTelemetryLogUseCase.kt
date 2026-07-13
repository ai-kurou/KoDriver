package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TelemetryLogRepository

class SaveTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    suspend operator fun invoke(
        createdAt: Long,
        simulatorId: String,
        readoutItemKey: String,
        telemetryJson: String,
    ) {
        repository.saveTelemetryLog(
            createdAt = createdAt,
            simulatorId = simulatorId,
            readoutItemKey = readoutItemKey,
            telemetryJson = telemetryJson,
        )
    }
}
