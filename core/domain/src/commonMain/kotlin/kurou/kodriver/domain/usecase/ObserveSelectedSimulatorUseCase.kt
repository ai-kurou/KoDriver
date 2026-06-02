package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

class ObserveSelectedSimulatorUseCase(private val repository: SimulatorPreferencesRepository) {
    operator fun invoke(): Flow<String?> = repository.selectedSimulator()
}
