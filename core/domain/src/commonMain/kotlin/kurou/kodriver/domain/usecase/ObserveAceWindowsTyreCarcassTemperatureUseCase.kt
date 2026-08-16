package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository

class ObserveAceWindowsTyreCarcassTemperatureUseCase(
    private val repository: AceWindowsTyreCarcassTemperatureRepository,
) {
    operator fun invoke(): Flow<AceWindowsTyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
