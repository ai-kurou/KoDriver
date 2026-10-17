package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassRepository {
    fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData>
}
