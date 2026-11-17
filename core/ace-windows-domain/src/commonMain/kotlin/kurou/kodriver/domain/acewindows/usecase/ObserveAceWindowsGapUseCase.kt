package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository

public class ObserveAceWindowsGapUseCase(
    private val repository: AceWindowsGapRepository,
) {
    public operator fun invoke(): Flow<AceWindowsGapData> = repository.gapStream()
}
