package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository

class ObserveTyreCarcassTemperatureUseCase(private val repository: TyreCarcassTemperatureRepository) {
    operator fun invoke(): Flow<TyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
