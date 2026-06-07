package kurou.kodriver.feature.readout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import org.koin.dsl.module

val fakeReadoutDataModule = module {
    single<SimulatorPreferencesRepository> { FakeSimulatorPreferencesRepositoryImpl() }
    single<ReadoutPreferencesRepository> { FakeReadoutPreferencesRepositoryImpl() }
    single<ProximityThresholdsRepository> { FakeProximityThresholdsRepositoryImpl() }
}

private class FakeSimulatorPreferencesRepositoryImpl : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow<String?>(null)
    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
}

private class FakeProximityThresholdsRepositoryImpl : ProximityThresholdsRepository {
    private val lateral = MutableStateFlow(5.0)
    private val longitudinal = MutableStateFlow(1.0)
    override fun observeLateralThresholdMeters(): Flow<Double> = lateral
    override fun observeLongitudinalThresholdMeters(): Flow<Double> = longitudinal
    override suspend fun saveLateralThresholdMeters(meters: Double) { lateral.value = meters }
    override suspend fun saveLongitudinalThresholdMeters(meters: Double) { longitudinal.value = meters }
}

private class FakeReadoutPreferencesRepositoryImpl : ReadoutPreferencesRepository {
    private val enabledStates = MutableStateFlow<Map<String, Map<String, Boolean>>>(emptyMap())
    private val orders = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        enabledStates.map { it[simulator] ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        enabledStates.update { all ->
            val current = all[simulator] ?: emptyMap()
            all + (simulator to (current + (label to enabled)))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<String>> =
        orders.map { it[simulator] ?: emptyList() }

    override suspend fun saveReadoutOrder(simulator: String, order: List<String>) {
        orders.update { it + (simulator to order) }
    }
}
