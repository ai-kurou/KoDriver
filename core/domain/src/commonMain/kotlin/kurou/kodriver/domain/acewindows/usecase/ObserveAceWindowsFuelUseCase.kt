package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository

class ObserveAceWindowsFuelUseCase(
    private val repository: AceWindowsFuelRepository,
) {
    operator fun invoke(): Flow<AceWindowsFuelData> = repository.fuelStream()
}
