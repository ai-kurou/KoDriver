package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import java.nio.ByteBuffer

/** プレイヤー車両(Telemetry)ブロックの +780〜+792 にある時間差(float, 秒)を読み取る。 */
internal object LmuWindowsCarGapMapper {
    private const val OFF_TIME_GAP_CAR_AHEAD = 780
    private const val OFF_TIME_GAP_CAR_BEHIND = 784
    private const val OFF_TIME_GAP_PLACE_AHEAD = 788
    private const val OFF_TIME_GAP_PLACE_BEHIND = 792

    /** 共有メモリの生の値のまま返す。車両不在時の値の意味は実機未確認のため解釈しない。 */
    internal fun readCarGap(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): LmuWindowsCarGapData =
        LmuWindowsCarGapData(
            timeGapCarAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_AHEAD).toDouble(),
            timeGapCarBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_BEHIND).toDouble(),
            timeGapPlaceAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_AHEAD).toDouble(),
            timeGapPlaceBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_BEHIND).toDouble(),
        )
}
