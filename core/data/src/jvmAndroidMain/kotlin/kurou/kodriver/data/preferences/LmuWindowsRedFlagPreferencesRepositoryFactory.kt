package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

/**
 * LmuWindowsRedFlagPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsRedFlagPreferencesRepository(directory: String): LmuWindowsRedFlagPreferencesRepository =
    LmuWindowsRedFlagPreferencesRepositoryImpl(createLmuWindowsRedFlagPreferencesDataStore(directory))
