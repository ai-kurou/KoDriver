package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository

class ObserveLmuWindowsPitStatusUseCase(
    private val repository: LmuWindowsPitStatusRepository,
) {
    operator fun invoke(): Flow<LmuWindowsPitStatusData> = repository.pitStatusStream()
}
