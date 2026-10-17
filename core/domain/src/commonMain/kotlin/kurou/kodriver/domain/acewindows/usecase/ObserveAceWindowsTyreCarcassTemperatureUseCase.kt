package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository

class ObserveAceWindowsTyreCarcassTemperatureUseCase(
    private val repository: AceWindowsTyreCarcassTemperatureRepository,
) {
    operator fun invoke(): Flow<AceWindowsTyreCarcassTemperatureData> = repository.tyreCarcassTemperatureStream()
}
