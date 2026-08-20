package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
