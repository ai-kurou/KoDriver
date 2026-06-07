package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

class ObserveLongitudinalThresholdUseCase(private val repository: ProximityThresholdsRepository) {
    operator fun invoke(): Flow<Double> = repository.observeLongitudinalThresholdMeters()
}
