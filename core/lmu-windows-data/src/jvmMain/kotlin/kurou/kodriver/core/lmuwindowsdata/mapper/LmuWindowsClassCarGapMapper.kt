package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import java.nio.ByteBuffer
import kotlin.math.floor

/** Scoring のラップ内時間を自車の推定ラップタイムで周回補正する。 */
internal object LmuWindowsClassCarGapMapper {
    private const val OFF_TIME_INTO_LAP = 464
    private const val OFF_ESTIMATED_LAP_TIME = 472

    /** 周回遅れ・周回先行も、順位ではなくコース上の位置関係で前後に含む。 */
    internal fun readClassCarGap(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): LmuWindowsClassCarGapData {
        val lapTime = buffer.getDouble(vehicleBase + OFF_ESTIMATED_LAP_TIME)
        val playerTime = buffer.getDouble(vehicleBase + OFF_TIME_INTO_LAP)
        if (!lapTime.isFinite() || lapTime <= 0) return LmuWindowsClassCarGapData(null, null)

        val playerClass = LmuWindowsMapper.readVehicleClassName(buffer, vehicleBase)
        val vehicleCount =
            buffer
                .getInt(LmuWindowsMapper.SCORING_BASE + LmuWindowsMapper.OFF_SCORING_NUM_VEHICLES)
                .coerceIn(0, LmuWindowsMapper.MAX_SCORING_VEHICLES)
        var gapAhead: Double? = null
        var gapBehind: Double? = null
        for (index in 0 until vehicleCount) {
            val otherBase = LmuWindowsMapper.VEHICLE_SCORING_BASE + index * LmuWindowsMapper.VEHICLE_SCORING_STRIDE
            if (!isSameClassOpponent(buffer, otherBase, playerClass)) continue
            val diff = buffer.getDouble(otherBase + OFF_TIME_INTO_LAP) - playerTime
            if (!diff.isFinite()) continue
            val ahead = diff - floor(diff / lapTime) * lapTime
            val behind = if (ahead > 0) lapTime - ahead else 0.0
            gapAhead = gapAhead?.let { minOf(it, ahead) } ?: ahead
            gapBehind = gapBehind?.let { minOf(it, behind) } ?: behind
        }
        return LmuWindowsClassCarGapData(gapAhead, gapBehind)
    }

    private fun isSameClassOpponent(
        buffer: ByteBuffer,
        otherBase: Int,
        playerClass: String,
    ): Boolean =
        buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_IS_PLAYER).toInt() == 0 &&
            buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_IN_GARAGE_STALL).toInt() == 0 &&
            LmuWindowsMapper.readVehicleClassName(buffer, otherBase) == playerClass
}
