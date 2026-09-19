package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository

class SaveTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    suspend operator fun invoke(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        telemetryJson: String,
    ) {
        repository.saveTelemetryLog(
            createdAt = createdAt,
            simulator = simulator,
            readoutItemKey = readoutItemKey,
            narratedText = narratedText,
            narrationOutcome = narrationOutcome,
            telemetryJson = telemetryJson,
        )
    }
}
