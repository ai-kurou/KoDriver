package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData

interface LmuWindowsVehicleApproachRepository {
    fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData>
}
