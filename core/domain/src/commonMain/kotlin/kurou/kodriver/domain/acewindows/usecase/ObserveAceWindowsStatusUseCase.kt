package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.repository.AceWindowsStatusRepository

class ObserveAceWindowsStatusUseCase(
    private val repository: AceWindowsStatusRepository,
) {
    operator fun invoke(): Flow<AceWindowsStatusData> = repository.statusStream()
}
