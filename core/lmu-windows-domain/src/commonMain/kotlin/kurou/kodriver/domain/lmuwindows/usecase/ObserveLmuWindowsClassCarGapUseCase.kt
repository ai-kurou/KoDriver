package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository

class ObserveLmuWindowsClassCarGapUseCase(
    private val repository: LmuWindowsClassCarGapRepository,
) {
    operator fun invoke(): Flow<LmuWindowsClassCarGapData> = repository.classCarGapStream()
}
