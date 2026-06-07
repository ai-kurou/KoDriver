package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

class ObserveLateralThresholdUseCase(private val repository: ProximityThresholdsRepository) {
    operator fun invoke(): Flow<Double> = repository.observeLateralThresholdMeters()
}
