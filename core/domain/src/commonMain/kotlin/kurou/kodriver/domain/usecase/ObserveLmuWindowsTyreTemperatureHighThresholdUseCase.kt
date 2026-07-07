package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeHighThresholdCelsius()
}
