package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

public class ObserveSelectedSimulatorUseCase(
    private val repository: SimulatorPreferencesRepository,
) {
    public operator fun invoke(): Flow<Simulator> = repository.selectedSimulator()
}
