package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class SaveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(celsius: Celsius): Unit = repository.saveHighThresholdCelsius(celsius)
}
