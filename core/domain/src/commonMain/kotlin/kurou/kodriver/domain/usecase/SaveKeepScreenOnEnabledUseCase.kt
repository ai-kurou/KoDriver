package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

class SaveKeepScreenOnEnabledUseCase(
    private val repository: KeepScreenOnEnabledRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveKeepScreenOn(enabled)
}
