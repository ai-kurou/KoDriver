package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class SaveAceWindowsBrakeTemperatureHighThresholdUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(celsius: Celsius): Unit = repository.saveHighThresholdCelsius(celsius)
}
