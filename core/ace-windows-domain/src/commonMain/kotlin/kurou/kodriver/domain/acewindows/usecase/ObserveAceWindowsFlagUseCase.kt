package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository

public class ObserveAceWindowsFlagUseCase(
    private val repository: AceWindowsFlagRepository,
) {
    public operator fun invoke(): Flow<AceWindowsFlagData> = repository.flagStream()
}
