package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class SaveGt7Ps5TyreTemperatureHighThresholdUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Celsius) = repository.saveHighThresholdCelsius(celsius)
}
