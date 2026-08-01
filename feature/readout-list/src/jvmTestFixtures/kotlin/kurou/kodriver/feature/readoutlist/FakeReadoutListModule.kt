package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。readout-list が使う :core:data の
 * Preferences Repository をインメモリの Fake 実装に差し替える。
 */
val fakeReadoutListModule = module {
    single<SimulatorPreferencesRepository> { FakeSimulatorPreferencesRepositoryImpl() }
    single<ReadoutPreferencesRepository> { FakeReadoutPreferencesRepositoryImpl() }
    single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
        FakeLmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl()
    }
    single<LmuWindowsFlagPreferencesRepository> { FakeLmuWindowsFlagPreferencesRepositoryImpl() }
    single<QueuePreferencesRepository> { FakeQueuePreferencesRepositoryImpl() }
}

private class FakeSimulatorPreferencesRepositoryImpl : SimulatorPreferencesRepository {
    private val flow = MutableStateFlow<Simulator?>(null)

    override fun selectedSimulator(): Flow<Simulator?> = flow

    override suspend fun saveSelectedSimulator(simulator: Simulator) {
        flow.update { simulator }
    }
}

private class FakeLmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl :
    LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    private val lateral = MutableStateFlow(5.0)
    private val longitudinal = MutableStateFlow(1.0)
    private val sustainedApproachDurationSeconds = MutableStateFlow(4)

    override fun observeLateralThresholdMeters(): Flow<Double> = lateral

    override fun observeLongitudinalThresholdMeters(): Flow<Double> = longitudinal

    override fun observeSustainedApproachDurationSeconds(): Flow<Int> = sustainedApproachDurationSeconds

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        lateral.update { meters }
    }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        longitudinal.update { meters }
    }

    override suspend fun saveSustainedApproachDurationSeconds(seconds: Int) {
        sustainedApproachDurationSeconds.update { seconds }
    }
}

private class FakeLmuWindowsFlagPreferencesRepositoryImpl : LmuWindowsFlagPreferencesRepository {
    private val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}

private class FakeQueuePreferencesRepositoryImpl : QueuePreferencesRepository {
    private val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeQueueEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveQueueEnabledState(key: ReadoutItemKey, enabled: Boolean) {
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
