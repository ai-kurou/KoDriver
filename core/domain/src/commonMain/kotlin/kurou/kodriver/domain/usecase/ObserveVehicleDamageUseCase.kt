package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.VehicleDamageRepository

class ObserveVehicleDamageUseCase(private val repository: VehicleDamageRepository) {
    operator fun invoke(): Flow<VehicleDamageData> = repository.vehicleDamageStream()
}
