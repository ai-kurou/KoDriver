package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

public class ObserveDynamicColorEnabledUseCase(
    private val repository: DynamicColorEnabledRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.dynamicColorEnabled()
}
