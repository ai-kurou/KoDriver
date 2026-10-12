package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperatureRepository

class ObserveLmuWindowsBrakeTemperatureUseCase(
    private val repository: LmuWindowsBrakeTemperatureRepository,
) {
    operator fun invoke(): Flow<LmuWindowsBrakeTemperatureData> = repository.brakeTemperatureStream()
}
