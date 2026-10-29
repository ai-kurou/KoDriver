package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import java.nio.ByteBuffer

/** Scoring の同クラス車両を対象に前後の時間差を求める。 */
internal object LmuWindowsClassCarGapMapper {
    /** 周回遅れ・周回先行も、順位ではなくコース上の位置関係で前後に含む。 */
    internal fun readClassCarGap(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): LmuWindowsClassCarGapData {
        val playerClass = LmuWindowsMapper.readVehicleClassName(buffer, vehicleBase)
        val gap =
            LmuWindowsScoringCarGapMapper.readCarGap(buffer, vehicleBase) { otherBase ->
                LmuWindowsMapper.readVehicleClassName(buffer, otherBase) == playerClass
            }
        return LmuWindowsClassCarGapData(gap.gapAheadSeconds, gap.gapBehindSeconds)
    }
}
