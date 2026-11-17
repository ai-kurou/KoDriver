package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository

public class ObserveAceWindowsTyreCarcassTemperatureUseCase(
    private val repository: AceWindowsTyreCarcassTemperatureRepository,
) {
    public operator fun invoke(): Flow<AceWindowsTyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
