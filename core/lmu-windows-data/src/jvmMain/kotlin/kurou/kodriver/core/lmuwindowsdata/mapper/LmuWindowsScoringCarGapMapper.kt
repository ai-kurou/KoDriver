package kurou.kodriver.core.lmuwindowsdata.mapper

import java.nio.ByteBuffer
import kotlin.math.floor

internal data class ScoringCarGap(
    val gapAheadSeconds: Double?,
    val gapBehindSeconds: Double?,
)

/** Scoring の位置関係から対象車両との前後の最小秒差を求める。 */
internal object LmuWindowsScoringCarGapMapper {
    private const val OFF_TIME_INTO_LAP = 464
    private const val OFF_ESTIMATED_LAP_TIME = 472

    internal fun readCarGap(
        buffer: ByteBuffer,
        vehicleBase: Int,
        includeVehicle: (Int) -> Boolean,
    ): ScoringCarGap {
        val lapTime = buffer.getDouble(vehicleBase + OFF_ESTIMATED_LAP_TIME)
        val playerTime = buffer.getDouble(vehicleBase + OFF_TIME_INTO_LAP)
        if (!lapTime.isFinite() || lapTime <= 0) return ScoringCarGap(null, null)

        var gapAhead: Double? = null
        var gapBehind: Double? = null
        for (otherBase in vehicleScoringBases(buffer)) {
            if (!isOpponent(buffer, otherBase) || !includeVehicle(otherBase)) continue
            val diff = buffer.getDouble(otherBase + OFF_TIME_INTO_LAP) - playerTime
            if (!diff.isFinite()) continue
            val ahead = diff - floor(diff / lapTime) * lapTime
            val behind = if (ahead > 0) lapTime - ahead else 0.0
            gapAhead = gapAhead?.let { minOf(it, ahead) } ?: ahead
            gapBehind = gapBehind?.let { minOf(it, behind) } ?: behind
        }
        return ScoringCarGap(gapAhead, gapBehind)
    }

    /** Scoring の台数を有効範囲に制限し、各車両の先頭オフセットを返す。 */
    internal fun vehicleScoringBases(buffer: ByteBuffer): IntProgression {
        val vehicleCount =
            buffer
                .getInt(LmuWindowsMapper.SCORING_BASE + LmuWindowsMapper.OFF_SCORING_NUM_VEHICLES)
                .coerceIn(0, LmuWindowsMapper.MAX_SCORING_VEHICLES)
        val start = LmuWindowsMapper.VEHICLE_SCORING_BASE
        val stride = LmuWindowsMapper.VEHICLE_SCORING_STRIDE
        return start until start + vehicleCount * stride step stride
    }

    private fun isOpponent(
        buffer: ByteBuffer,
        otherBase: Int,
    ): Boolean =
        buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_IS_PLAYER).toInt() == 0 &&
            buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_IN_GARAGE_STALL).toInt() == 0
}
