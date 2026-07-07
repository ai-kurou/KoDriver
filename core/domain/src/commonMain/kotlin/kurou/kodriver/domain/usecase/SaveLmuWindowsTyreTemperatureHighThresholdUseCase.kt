package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class SaveLmuWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Int) = repository.saveHighThresholdCelsius(celsius)
}
