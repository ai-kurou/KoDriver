package kurou.kodriver.domain.telemetrylog.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.telemetrylog.model.NarrationOutcome
import kurou.kodriver.domain.telemetrylog.model.NarrationSource
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog
import kurou.kodriver.domain.telemetrylog.model.TelemetryLogDetail

public interface TelemetryLogRepository {
    public fun observeTelemetryLogs(): Flow<List<TelemetryLog>>

    public fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?>

    /** 実際に読み上げられた最新のログを観測する。読み上げされなかったログは含まない。 */
    public fun observeLatestNarratedTelemetryLog(): Flow<TelemetryLog?>

    public suspend fun saveTelemetryLog(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        narratedText: String,
        narrationOutcome: NarrationOutcome,
        narrationSource: NarrationSource,
        telemetryJson: String,
    )

    public suspend fun deleteTelemetryLogsBySimulator(simulator: Simulator)

    public suspend fun deleteTelemetryLog(id: Long)
}
