package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassRepository {
    fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData>
}
