package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

class ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
