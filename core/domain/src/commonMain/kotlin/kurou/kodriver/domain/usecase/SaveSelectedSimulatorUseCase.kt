package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

class SaveSelectedSimulatorUseCase(private val repository: SimulatorPreferencesRepository) {
    suspend operator fun invoke(simulator: Simulator) = repository.saveSelectedSimulator(simulator)
}
