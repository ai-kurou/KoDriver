package kurou.kodriver.data.preferences

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

/**
 * DebugStateCardOrderPreferences Repository の永続化実装を、指定したシミュレーター専用に生成する。
 */
fun createDebugStateCardOrderPreferencesRepository(
    directory: String,
    simulator: Simulator,
): DebugStateCardOrderPreferencesRepository =
    DebugStateCardOrderPreferencesRepositoryImpl(createDebugStateCardOrderPreferencesDataStore(directory, simulator))
