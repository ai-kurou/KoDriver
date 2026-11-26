package kurou.kodriver.data.preferences

import kurou.kodriver.domain.debug.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.model.Simulator

/**
 * DebugStateCardOrderPreferences Repository の永続化実装を、指定したシミュレーター専用に生成する。
 */
internal fun createDebugStateCardOrderPreferencesRepository(
    directory: String,
    simulator: Simulator,
): DebugStateCardOrderPreferencesRepository =
    DebugStateCardOrderPreferencesRepositoryImpl(createDebugStateCardOrderPreferencesDataStore(directory, simulator))
