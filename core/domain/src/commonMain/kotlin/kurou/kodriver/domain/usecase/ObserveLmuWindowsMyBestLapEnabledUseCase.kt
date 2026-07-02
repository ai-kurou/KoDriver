package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository

class ObserveLmuWindowsMyBestLapEnabledUseCase(
    private val repository: LmuWindowsMyBestLapEnabledRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
