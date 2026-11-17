package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

public class SaveSelectedSimulatorUseCase(
    private val repository: SimulatorPreferencesRepository,
) {
    public suspend operator fun invoke(simulator: Simulator): Unit = repository.saveSelectedSimulator(simulator)
}
