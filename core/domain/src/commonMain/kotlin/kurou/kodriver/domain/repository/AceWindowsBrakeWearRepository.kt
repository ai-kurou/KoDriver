package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsBrakeWearData

/**
 * Assetto Corsa EVO の Windows 共有メモリからブレーキパッド・ディスクの摩耗指標を読む Repository。
 */
interface AceWindowsBrakeWearRepository {
    /** 4輪分のブレーキパッド・ディスク摩耗指標を継続配信する cold Flow。 */
    fun brakeWearStream(): Flow<AceWindowsBrakeWearData>
}
