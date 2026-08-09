package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVehicleDamageData

interface LmuWindowsVehicleDamageRepository {
    fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData>
}
