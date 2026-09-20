package kurou.kodriver.data.telemetrylog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TelemetryLogDao {
    @Query("SELECT * FROM telemetry_logs ORDER BY createdAt DESC, id DESC")
    fun observeTelemetryLogs(): Flow<List<TelemetryLogEntity>>

    @Query("SELECT * FROM telemetry_logs WHERE id = :id")
    fun observeTelemetryLog(id: Long): Flow<TelemetryLogEntity?>

    /**
     * 実際に読み上げられた最新のログを観測する。
     *
     * 読み上げされなかったログ（[excludedNarrationOutcome]）は、音が鳴っていないため対象外にする。
     * Room の [Query] には定数しか書けないため、除外する id は呼び出し側から値として渡す。
     */
    @Query(
        """
        SELECT * FROM telemetry_logs
        WHERE narrationOutcome != :excludedNarrationOutcome
        ORDER BY createdAt DESC, id DESC
        LIMIT 1
        """,
    )
    fun observeLatestNarratedTelemetryLog(excludedNarrationOutcome: String): Flow<TelemetryLogEntity?>

    @Query(
        """
        SELECT * FROM telemetry_logs
        WHERE createdAt < :createdAt OR (createdAt = :createdAt AND id < :id)
        ORDER BY createdAt DESC, id DESC
        LIMIT 1
        """,
    )
    fun observePreviousTelemetryLog(
        createdAt: Long,
        id: Long,
    ): Flow<TelemetryLogEntity?>

    @Insert
    suspend fun insert(log: TelemetryLogEntity)

    @Query("DELETE FROM telemetry_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM telemetry_logs WHERE id = :id")
    suspend fun delete(id: Long)
}
