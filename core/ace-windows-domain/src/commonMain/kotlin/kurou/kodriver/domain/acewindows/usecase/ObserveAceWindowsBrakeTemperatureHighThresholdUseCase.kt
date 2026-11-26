package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class ObserveAceWindowsBrakeTemperatureHighThresholdUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
