package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureEnabledRepository

class ObserveLmuWindowsTyreTemperatureEnabledUseCase(
    private val repository: LmuWindowsTyreTemperatureEnabledRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
