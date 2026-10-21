package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository

class ObserveAceWindowsFlagUseCase(
    private val repository: AceWindowsFlagRepository,
) {
    operator fun invoke(): Flow<AceWindowsFlagData> = repository.flagStream()
}
