package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

/**
 * DebugStateCardOrderPreferences Repository の永続化実装を生成する。
 */
fun createDebugStateCardOrderPreferencesRepository(directory: String): DebugStateCardOrderPreferencesRepository =
    DebugStateCardOrderPreferencesRepositoryImpl(createDebugStateCardOrderPreferencesDataStore(directory))
