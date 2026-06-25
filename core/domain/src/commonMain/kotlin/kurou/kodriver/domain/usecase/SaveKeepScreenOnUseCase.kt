package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository

class SaveKeepScreenOnUseCase(private val repository: KeepScreenOnPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveKeepScreenOn(enabled)
}
