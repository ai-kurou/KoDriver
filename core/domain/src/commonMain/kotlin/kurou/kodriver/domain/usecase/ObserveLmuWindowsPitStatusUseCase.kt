package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository

class ObserveLmuWindowsPitStatusUseCase(
    private val repository: LmuWindowsPitStatusRepository,
) {
    operator fun invoke(): Flow<LmuWindowsPitStatusData> = repository.pitStatusStream()
}
