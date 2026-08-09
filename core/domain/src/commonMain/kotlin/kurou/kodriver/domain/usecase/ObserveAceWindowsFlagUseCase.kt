package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsFlagData
import kurou.kodriver.domain.repository.AceWindowsFlagRepository

class ObserveAceWindowsFlagUseCase(
    private val repository: AceWindowsFlagRepository,
) {
    operator fun invoke(): Flow<AceWindowsFlagData> = repository.flagStream()
}
