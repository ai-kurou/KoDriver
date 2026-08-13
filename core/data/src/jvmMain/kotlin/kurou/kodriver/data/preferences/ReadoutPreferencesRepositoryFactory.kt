package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

/**
 * ReadoutPreferences Repository の永続化実装を生成する。
 */
fun createReadoutPreferencesRepository(directory: String): ReadoutPreferencesRepository =
    ReadoutPreferencesRepositoryImpl(createReadoutPreferencesDataStore(directory))
