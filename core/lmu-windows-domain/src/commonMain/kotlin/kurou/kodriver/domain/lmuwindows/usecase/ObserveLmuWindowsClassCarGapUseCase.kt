package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository

public class ObserveLmuWindowsClassCarGapUseCase(
    private val repository: LmuWindowsClassCarGapRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsClassCarGapData> = repository.classCarGapStream()
}
