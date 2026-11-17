package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData

public interface LmuWindowsVehicleApproachRepository {
    public fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData>
}
