package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ProximityThresholdsRepository

class SaveLongitudinalThresholdUseCase(private val repository: ProximityThresholdsRepository) {
    suspend operator fun invoke(meters: Double) = repository.saveLongitudinalThresholdMeters(meters)
}
