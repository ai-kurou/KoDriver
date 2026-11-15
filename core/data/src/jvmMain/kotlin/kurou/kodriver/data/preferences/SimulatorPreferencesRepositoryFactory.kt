package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

/**
 * SimulatorPreferences Repository の永続化実装を生成する。
 */
internal fun createSimulatorPreferencesRepository(directory: String): SimulatorPreferencesRepository =
    SimulatorPreferencesRepositoryImpl(createSimulatorPreferencesDataStore(directory))
