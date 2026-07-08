package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository

class ObserveLmuWindowsMyBestLapEnabledUseCase(
    private val repository: LmuWindowsMyBestLapEnabledRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled().map { it ?: false }
}
