package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository

class ObserveLmuWindowsTyreCarcassTemperatureUseCase(
    private val repository: LmuWindowsTyreCarcassTemperatureRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
