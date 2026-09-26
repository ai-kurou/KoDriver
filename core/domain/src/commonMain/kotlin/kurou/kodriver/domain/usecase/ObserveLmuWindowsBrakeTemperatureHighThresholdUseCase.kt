package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperaturePreferencesRepository

class ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsBrakeTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeHighThresholdCelsius()
}
