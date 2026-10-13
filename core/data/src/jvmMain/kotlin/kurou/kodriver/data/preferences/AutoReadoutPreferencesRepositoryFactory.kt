package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository

/**
 * AutoReadoutPreferences Repository の永続化実装を生成する。
 */
fun createAutoReadoutPreferencesRepository(directory: String): AutoReadoutPreferencesRepository =
    AutoReadoutPreferencesRepositoryImpl(createAutoReadoutPreferencesDataStore(directory))
