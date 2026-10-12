package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData

interface LmuWindowsVehicleDamageRepository {
    fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData>
}
