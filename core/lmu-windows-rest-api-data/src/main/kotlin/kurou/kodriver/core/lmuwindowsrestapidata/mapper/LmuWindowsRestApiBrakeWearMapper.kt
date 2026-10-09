package kurou.kodriver.core.lmuwindowsrestapidata.mapper

import kurou.kodriver.core.lmuwindowsrestapidata.dto.RepairAndRefuelResponseDto
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.model.WheelIndex

internal object LmuWindowsRestApiBrakeWearMapper {
    /**
     * `wearables.brakes` を [LmuWindowsBrakeWearData] へ変換する。
     * 4 輪分（FL, FR, RL, RR の順）が揃っていない場合は null を返す。
     */
    fun toBrakeWear(dto: RepairAndRefuelResponseDto): LmuWindowsBrakeWearData? {
        val brakes = dto.wearables?.brakes ?: return null
        if (brakes.size != WheelIndex.entries.size) return null
        return LmuWindowsBrakeWearData(
            wheels = WheelIndex.entries.associateWith { BrakeThicknessMeters(brakes[it.ordinal].toFloat()) },
        )
    }
}
