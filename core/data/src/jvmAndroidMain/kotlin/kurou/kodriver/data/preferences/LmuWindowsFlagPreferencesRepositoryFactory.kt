package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository

/**
 * LmuWindowsFlagPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsFlagPreferencesRepository(directory: String): LmuWindowsFlagPreferencesRepository =
    LmuWindowsFlagPreferencesRepositoryImpl(createLmuWindowsFlagPreferencesDataStore(directory))
