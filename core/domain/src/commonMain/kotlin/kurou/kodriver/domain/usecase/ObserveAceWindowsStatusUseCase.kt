package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsStatusData
import kurou.kodriver.domain.repository.AceWindowsStatusRepository

class ObserveAceWindowsStatusUseCase(
    private val repository: AceWindowsStatusRepository,
) {
    operator fun invoke(): Flow<AceWindowsStatusData> = repository.statusStream()
}
