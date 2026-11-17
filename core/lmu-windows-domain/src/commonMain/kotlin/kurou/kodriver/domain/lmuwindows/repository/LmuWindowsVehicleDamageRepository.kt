package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData

public interface LmuWindowsVehicleDamageRepository {
    public fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData>
}
