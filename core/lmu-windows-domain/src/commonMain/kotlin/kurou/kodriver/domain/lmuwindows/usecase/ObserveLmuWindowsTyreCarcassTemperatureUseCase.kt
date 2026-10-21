package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository

class ObserveLmuWindowsTyreCarcassTemperatureUseCase(
    private val repository: LmuWindowsTyreCarcassTemperatureRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
