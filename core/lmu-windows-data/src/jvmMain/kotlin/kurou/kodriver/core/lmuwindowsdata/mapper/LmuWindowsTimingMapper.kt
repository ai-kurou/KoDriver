package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import java.nio.ByteBuffer
import kotlin.math.roundToLong

/**
 * LMU 共有メモリの Scoring / Telemetry から [LmuWindowsTimingData] を組み立てる。
 * オフセットの基準値は [LmuWindowsMapper] を参照する。
 */
internal object LmuWindowsTimingMapper {
    private const val OFF_SCORING_CURRENT_ET = 68
    private const val OFF_SCORING_MAX_LAPS = 84
    private const val OFF_SCORING_BEST_LAP_TIME = 144
    private const val OFF_SCORING_LAST_LAP_TIME = 168
    private const val OFF_SCORING_LAP_START_ET = 256
    private const val OFF_SCORING_BEST_LAP_SECTOR1 = 576
    private const val OFF_SCORING_BEST_LAP_SECTOR2 = 580
    private const val OFF_LAP_NUMBER = 20
    private const val MILLIS_PER_SECOND = 1_000

    fun map(
        buffer: ByteBuffer,
        vehicleBase: Int,
        vehicleScoringBase: Int?,
    ): LmuWindowsTimingData =
        LmuWindowsTimingData(
            currentLapTimeMs = currentLapTimeMs(buffer, vehicleScoringBase),
            lastLapTimeMs = vehicleScoringBase.readDoubleSecondsAsMillis(buffer, OFF_SCORING_LAST_LAP_TIME),
            bestLapTimeMs = vehicleScoringBase.readDoubleSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_TIME),
            sector1Ms = vehicleScoringBase.readFloatSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_SECTOR1),
            sector1And2Ms = vehicleScoringBase.readFloatSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_SECTOR2),
            currentLap = buffer.getInt(vehicleBase + OFF_LAP_NUMBER),
            maxLaps = buffer.getInt(LmuWindowsMapper.SCORING_BASE + OFF_SCORING_MAX_LAPS),
        )

    private fun currentLapTimeMs(
        buffer: ByteBuffer,
        vehicleScoringBase: Int?,
    ): Long {
        if (vehicleScoringBase == null) return 0L
        val currentSessionTime = buffer.getDouble(LmuWindowsMapper.SCORING_BASE + OFF_SCORING_CURRENT_ET)
        val lapStartSessionTime = buffer.getDouble(vehicleScoringBase + OFF_SCORING_LAP_START_ET)
        return secondsToMillis(currentSessionTime - lapStartSessionTime)
    }

    private fun Int?.readDoubleSecondsAsMillis(
        buffer: ByteBuffer,
        offset: Int,
    ): Long = if (this == null) 0L else secondsToMillis(buffer.getDouble(this + offset))

    private fun Int?.readFloatSecondsAsMillis(
        buffer: ByteBuffer,
        offset: Int,
    ): Long = if (this == null) 0L else secondsToMillis(buffer.getFloat(this + offset).toDouble())

    private fun secondsToMillis(seconds: Double): Long =
        if (seconds.isFinite() && seconds > 0.0) {
            (seconds * MILLIS_PER_SECOND).roundToLong()
        } else {
            0L
        }
}
