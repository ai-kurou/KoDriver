package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureEnabledRepository

class SaveLmuWindowsTyreTemperatureEnabledUseCase(
    private val repository: LmuWindowsTyreTemperatureEnabledRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveEnabled(enabled)
}
