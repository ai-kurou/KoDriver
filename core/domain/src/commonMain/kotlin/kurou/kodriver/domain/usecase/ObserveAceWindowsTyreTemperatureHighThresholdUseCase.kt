package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

class ObserveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
