package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository

class ObserveLmuWindowsCarGapUseCase(
    private val repository: LmuWindowsCarGapRepository,
) {
    operator fun invoke(): Flow<LmuWindowsCarGapData> = repository.carGapStream()
}
