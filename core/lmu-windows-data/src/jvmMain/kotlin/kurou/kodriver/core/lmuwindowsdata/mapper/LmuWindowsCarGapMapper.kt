package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import java.nio.ByteBuffer

/** プレイヤー車両(Telemetry)ブロックの +780〜+792 にある時間差(float, 秒)を読み取る。 */
internal object LmuWindowsCarGapMapper {
    private const val OFF_TIME_GAP_CAR_AHEAD = 780
    private const val OFF_TIME_GAP_CAR_BEHIND = 784
    private const val OFF_TIME_GAP_PLACE_AHEAD = 788
    private const val OFF_TIME_GAP_PLACE_BEHIND = 792

    /** Telemetry の参考用生値と、Scoring から算出した全体ギャップを返す。 */
    internal fun readCarGap(
        buffer: ByteBuffer,
        vehicleBase: Int,
        scoringBase: Int? = null,
    ): LmuWindowsCarGapData {
        val gap = scoringBase?.let { LmuWindowsScoringCarGapMapper.readCarGap(buffer, it) { true } }
        return LmuWindowsCarGapData(
            timeGapCarAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_AHEAD).toDouble(),
            timeGapCarBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_BEHIND).toDouble(),
            timeGapPlaceAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_AHEAD).toDouble(),
            timeGapPlaceBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_BEHIND).toDouble(),
            overallGapAheadSeconds = gap?.gapAheadSeconds,
            overallGapBehindSeconds = gap?.gapBehindSeconds,
        )
    }
}
