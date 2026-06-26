package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository

class SaveGt7Ps5RemainingFuelLapsEnabledUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsEnabledRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveEnabled(enabled)
}
