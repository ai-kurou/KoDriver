package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData

/**
 * Assetto Corsa EVO の Windows 共有メモリから周辺車両との位置関係を読む Repository。
 */
interface AceWindowsVehicleApproachRepository {
    /** 周辺車両との位置関係を継続配信する cold Flow。 */
    fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData>
}
