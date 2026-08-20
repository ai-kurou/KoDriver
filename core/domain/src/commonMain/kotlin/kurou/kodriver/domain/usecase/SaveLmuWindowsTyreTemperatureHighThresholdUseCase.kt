package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class SaveLmuWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Celsius) = repository.saveHighThresholdCelsius(celsius)
}
