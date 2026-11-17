package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

public class SaveDynamicColorEnabledUseCase(
    private val repository: DynamicColorEnabledRepository,
) {
    public suspend operator fun invoke(enabled: Boolean): Unit = repository.saveDynamicColorEnabled(enabled)
}
