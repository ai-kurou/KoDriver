package kurou.kodriver.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
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

internal fun TelemetryLogEntity.toDomain(): TelemetryLog =
    TelemetryLog(
        id = id,
        createdAt = createdAt,
        simulatorId = simulatorId,
        readoutItemKey = readoutItemKey,
        telemetryJson = telemetryJson,
    )

internal fun TelemetryLog.toEntity(): TelemetryLogEntity =
    TelemetryLogEntity(
        id = id,
        createdAt = createdAt,
        simulatorId = simulatorId,
        readoutItemKey = readoutItemKey,
        telemetryJson = telemetryJson,
    )
