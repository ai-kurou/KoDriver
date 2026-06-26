package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class FakeSimulatorPreferencesRepository(
    initial: Simulator? = null,
) : SimulatorPreferencesRepository {
    val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<Simulator?> = flow
    override suspend fun saveSelectedSimulator(simulator: Simulator) { flow.value = simulator }
}
