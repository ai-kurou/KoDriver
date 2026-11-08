package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

/** 選択中のシミュレーターを保持するだけのテスト用Repository。 */
internal class FakeSelectedSimulatorRepository(
    initial: Simulator = Simulator.LmuWindows,
) : SimulatorPreferencesRepository {
    val selected = MutableStateFlow(initial)

    override fun selectedSimulator(): Flow<Simulator> = selected

    override suspend fun saveSelectedSimulator(simulator: Simulator) {
        selected.value = simulator
    }
}
