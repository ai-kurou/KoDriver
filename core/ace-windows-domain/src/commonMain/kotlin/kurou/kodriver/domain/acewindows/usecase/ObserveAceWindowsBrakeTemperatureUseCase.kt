package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsBrakeTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperatureRepository

public class ObserveAceWindowsBrakeTemperatureUseCase(
    private val repository: AceWindowsBrakeTemperatureRepository,
) {
    public operator fun invoke(): Flow<AceWindowsBrakeTemperatureData> = repository.brakeTemperatureStream()
}
