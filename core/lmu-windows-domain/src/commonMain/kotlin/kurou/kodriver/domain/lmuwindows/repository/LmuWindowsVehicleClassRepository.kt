package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData

public interface LmuWindowsVehicleClassRepository {
    public fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData>
}
