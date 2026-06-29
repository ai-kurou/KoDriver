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

    @Insert
    suspend fun insert(log: TelemetryLogEntity)
}
