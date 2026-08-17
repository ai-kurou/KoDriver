package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

class SaveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Int) = repository.saveHighThresholdCelsius(celsius)
}
