package kurou.kodriver.data.preferences

import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository

/**
 * AutoReadoutPreferences Repository の永続化実装を生成する。
 */
internal fun createAutoReadoutPreferencesRepository(directory: String): AutoReadoutPreferencesRepository =
    AutoReadoutPreferencesRepositoryImpl(createAutoReadoutPreferencesDataStore(directory))
