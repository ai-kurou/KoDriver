package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository

public class ObserveLmuWindowsTyreDetachedUseCase(
    private val repository: LmuWindowsTyreDetachedRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsTyreDetachedData> = repository.tyreDetachedStream()
}
