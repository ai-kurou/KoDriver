package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository

public class ObserveLmuWindowsRaceFlagsUseCase(
    private val repository: LmuWindowsFlagRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsRaceFlagsData> = repository.flagStream()
}
