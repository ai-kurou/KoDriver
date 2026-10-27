package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository

class ObserveAceWindowsGapUseCase(
    private val repository: AceWindowsGapRepository,
) {
    operator fun invoke(): Flow<AceWindowsGapData> = repository.gapStream()
}
