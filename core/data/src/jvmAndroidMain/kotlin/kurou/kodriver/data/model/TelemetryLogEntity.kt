package kurou.kodriver.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.TelemetryLog

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
