package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import java.nio.ByteBuffer

/** Scoring の同クラス車両を対象に前後の時間差とクラス内順位を求める。 */
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
        val playerPlace = buffer.get(vehicleBase + LmuWindowsMapper.OFF_SCORING_PLACE).toInt() and 0xFF
        if (playerPlace == 0) return LmuWindowsClassCarGapData(gap.gapAheadSeconds, gap.gapBehindSeconds)

        var classPosition = 1
        var classVehicleCount = 1
        for (otherBase in LmuWindowsScoringCarGapMapper.vehicleScoringBases(buffer)) {
            if (
                otherBase == vehicleBase ||
                buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_IN_GARAGE_STALL).toInt() != 0 ||
                LmuWindowsMapper.readVehicleClassName(buffer, otherBase) != playerClass
            ) {
                continue
            }
            classVehicleCount++
            val otherPlace = buffer.get(otherBase + LmuWindowsMapper.OFF_SCORING_PLACE).toInt() and 0xFF
            if (otherPlace < playerPlace) classPosition++
        }
        return LmuWindowsClassCarGapData(
            gapAheadSeconds = gap.gapAheadSeconds,
            gapBehindSeconds = gap.gapBehindSeconds,
            classPosition = classPosition,
            classVehicleCount = classVehicleCount,
        )
    }
}
