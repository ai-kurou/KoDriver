package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository

public class ObserveLmuWindowsBrakeTemperatureUseCase(
    private val repository: LmuWindowsBrakeTemperatureRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsBrakeTemperatureData> = repository.brakeTemperatureStream()
}
