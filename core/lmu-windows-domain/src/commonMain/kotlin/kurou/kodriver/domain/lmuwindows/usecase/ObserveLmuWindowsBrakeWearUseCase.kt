package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository

public class ObserveLmuWindowsBrakeWearUseCase(
    private val repository: LmuWindowsBrakeWearRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsBrakeWearData?> = repository.brakeWearStream()
}
