package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.repository.LmuWindowsTyreDetachedRepository

class ObserveLmuWindowsTyreDetachedUseCase(
    private val repository: LmuWindowsTyreDetachedRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTyreDetachedData> = repository.tyreDetachedStream()
}
