package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData

interface LmuWindowsVehicleApproachRepository {
    fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData>
}
