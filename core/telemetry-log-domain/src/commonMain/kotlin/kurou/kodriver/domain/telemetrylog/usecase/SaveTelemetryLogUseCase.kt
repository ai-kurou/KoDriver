package kurou.kodriver.domain.telemetrylog.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.telemetrylog.model.NarrationOutcome
import kurou.kodriver.domain.telemetrylog.model.NarrationSource
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository

public class SaveTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    public suspend operator fun invoke(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        narrationSource: NarrationSource,
        telemetryJson: String,
    ) {
        repository.saveTelemetryLog(
            createdAt = createdAt,
            simulator = simulator,
            readoutItemKey = readoutItemKey,
            narratedText = narratedText,
            narrationOutcome = narrationOutcome,
            narrationSource = narrationSource,
            telemetryJson = telemetryJson,
        )
    }
}
