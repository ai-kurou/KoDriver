package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal class FakeSimulatorPreferencesRepository(
    initial: String? = null,
) : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
}
