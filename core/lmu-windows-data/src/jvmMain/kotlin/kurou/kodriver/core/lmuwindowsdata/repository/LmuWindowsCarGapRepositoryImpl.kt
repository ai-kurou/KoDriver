package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import java.nio.ByteBuffer

private const val OFF_TIME_GAP_CAR_AHEAD = 780
private const val OFF_TIME_GAP_CAR_BEHIND = 784
private const val OFF_TIME_GAP_PLACE_AHEAD = 788
private const val OFF_TIME_GAP_PLACE_BEHIND = 792

internal class LmuWindowsCarGapRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsCarGapRepository {
    override fun carGapStream(): Flow<LmuWindowsCarGapData> =
        source.bufferFlow.mapNotNull {
            readCarGap(it)
        }

    /** プレイヤー車両の時間差（秒）を共有メモリの生の値のまま読み取る。 */
    private fun readCarGap(buffer: ByteBuffer): LmuWindowsCarGapData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        return LmuWindowsCarGapData(
            timeGapCarAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_AHEAD).toDouble(),
            timeGapCarBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_CAR_BEHIND).toDouble(),
            timeGapPlaceAheadSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_AHEAD).toDouble(),
            timeGapPlaceBehindSeconds = buffer.getFloat(vehicleBase + OFF_TIME_GAP_PLACE_BEHIND).toDouble(),
        )
    }
}
