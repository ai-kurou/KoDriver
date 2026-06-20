package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.ProximityThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import org.koin.dsl.module

val fakeReadoutListModule = module {
    single<SimulatorPreferencesRepository> { FakeSimulatorPreferencesRepositoryImpl() }
    single<ReadoutPreferencesRepository> { FakeReadoutPreferencesRepositoryImpl() }
    single<ProximityThresholdsPreferencesRepository> { FakeProximityThresholdsPreferencesRepositoryImpl() }
    single<FlagPreferencesRepository> { FakeFlagPreferencesRepositoryImpl() }
}

private class FakeSimulatorPreferencesRepositoryImpl : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow<String?>(null)
    override fun selectedSimulator(): Flow<String?> = flow
    override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
}

private class FakeProximityThresholdsPreferencesRepositoryImpl : ProximityThresholdsPreferencesRepository {
    private val lateral = MutableStateFlow(5.0)
    private val longitudinal = MutableStateFlow(1.0)
    override fun observeLateralThresholdMeters(): Flow<Double> = lateral
    override fun observeLongitudinalThresholdMeters(): Flow<Double> = longitudinal
    override suspend fun saveLateralThresholdMeters(meters: Double) { lateral.update { meters } }
    override suspend fun saveLongitudinalThresholdMeters(meters: Double) { longitudinal.update { meters } }
}

private class FakeFlagPreferencesRepositoryImpl : FlagPreferencesRepository {
    private val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states
    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}

private class FakeReadoutPreferencesRepositoryImpl : ReadoutPreferencesRepository {
    private val enabledStates = MutableStateFlow<Map<String, Map<ReadoutItemKey, Boolean>>>(emptyMap())
    private val orders = MutableStateFlow<Map<String, List<ReadoutItemKey>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        enabledStates.map { it[simulator] ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) {
        enabledStates.update { all ->
            val current = all[simulator] ?: emptyMap()
            all + (simulator to (current + (key to enabled)))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        orders.map { it[simulator] ?: emptyList() }

    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) {
        orders.update { it + (simulator to order) }
    }
}
