package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsFuelData
import kurou.kodriver.domain.repository.AceWindowsFuelRepository

class ObserveAceWindowsFuelUseCase(
    private val repository: AceWindowsFuelRepository,
) {
    operator fun invoke(): Flow<AceWindowsFuelData> = repository.fuelStream()
}
