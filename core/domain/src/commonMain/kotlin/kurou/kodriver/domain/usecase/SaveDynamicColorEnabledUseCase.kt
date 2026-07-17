package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

class SaveDynamicColorEnabledUseCase(private val repository: DynamicColorEnabledRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveDynamicColorEnabled(enabled)
}
