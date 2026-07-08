package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

class SaveKeepScreenOnUseCase(private val repository: KeepScreenOnEnabledRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveKeepScreenOn(enabled)
}
