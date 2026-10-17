package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository

class ObserveLmuWindowsBrakeTemperatureUseCase(
    private val repository: LmuWindowsBrakeTemperatureRepository,
) {
    operator fun invoke(): Flow<LmuWindowsBrakeTemperatureData> = repository.brakeTemperatureStream()
}
