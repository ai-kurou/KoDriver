package kurou.kodriver.data.telemetrylog

import androidx.room.Entity
import androidx.room.PrimaryKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog

@Entity(tableName = "telemetry_logs")
internal data class TelemetryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long,
    val simulatorId: String,
    val readoutItemKey: String,
    val telemetryJson: String,
)

internal fun TelemetryLogEntity.toDomain(): TelemetryLog? {
    val simulator = Simulator.fromId(simulatorId) ?: return null
    val readoutItemKey = ReadoutItemKey.fromValue(readoutItemKey) ?: return null
    return TelemetryLog(
        id = id,
        createdAt = createdAt,
        simulator = simulator,
        readoutItemKey = readoutItemKey,
        telemetryJson = telemetryJson,
    )
}
