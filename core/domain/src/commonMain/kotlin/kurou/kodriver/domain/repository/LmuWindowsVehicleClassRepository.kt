package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVehicleClassData

interface LmuWindowsVehicleClassRepository {
    fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData>
}
