package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

class SaveSelectedSimulatorUseCase(private val repository: SimulatorPreferencesRepository) {
    suspend operator fun invoke(simulator: String) = repository.saveSelectedSimulator(simulator)
}
