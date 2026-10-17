package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData

interface LmuWindowsVehicleDamageRepository {
    fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData>
}
