package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

class ObserveDynamicColorEnabledUseCase(private val repository: DynamicColorEnabledRepository) {
    operator fun invoke(): Flow<Boolean> = repository.dynamicColorEnabled()
}
