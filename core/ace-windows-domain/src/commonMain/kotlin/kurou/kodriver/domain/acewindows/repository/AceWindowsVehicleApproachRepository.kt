package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData

/**
 * Assetto Corsa EVO の Windows 共有メモリから周辺車両との位置関係を読む Repository。
 */
public interface AceWindowsVehicleApproachRepository {
    /** 周辺車両との位置関係を継続配信する cold Flow。 */
    public fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData>
}
