package kurou.kodriver.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.TelemetryLogEntity

@Dao
internal interface TelemetryLogDao {
    @Query("SELECT * FROM telemetry_logs ORDER BY createdAt DESC, id DESC")
    fun observeTelemetryLogs(): Flow<List<TelemetryLogEntity>>

    @Query("SELECT * FROM telemetry_logs WHERE id = :id")
    fun observeTelemetryLog(id: Long): Flow<TelemetryLogEntity?>

    @Query(
        """
        SELECT * FROM telemetry_logs
        WHERE createdAt < :createdAt OR (createdAt = :createdAt AND id < :id)
        ORDER BY createdAt DESC, id DESC
        LIMIT 1
        """,
    )
    fun observePreviousTelemetryLog(createdAt: Long, id: Long): Flow<TelemetryLogEntity?>

    @Insert
    suspend fun insert(log: TelemetryLogEntity)
}
