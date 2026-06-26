package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7RemainingFuelLapsPreferencesRepository

class SaveGt7RemainingFuelLapsEnabledUseCase(
    private val repository: Gt7RemainingFuelLapsPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveEnabled(enabled)
}
