package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleDamageData

interface VehicleDamageRepository {
    fun vehicleDamageStream(): Flow<VehicleDamageData>
}
