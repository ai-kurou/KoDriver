package kurou.kodriver.data

import kurou.kodriver.data.datasource.createDebugStateCardOrderPreferencesDataStore
import kurou.kodriver.data.repository.DebugStateCardOrderPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

/**
 * DebugStateCardOrderPreferences Repository の永続化実装を生成する。
 */
fun createDebugStateCardOrderPreferencesRepository(directory: String): DebugStateCardOrderPreferencesRepository =
    DebugStateCardOrderPreferencesRepositoryImpl(createDebugStateCardOrderPreferencesDataStore(directory))
