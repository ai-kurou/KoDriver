package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository

/**
 * LmuWindowsRainPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsRainPreferencesRepository(directory: String): LmuWindowsRainPreferencesRepository =
    LmuWindowsRainPreferencesRepositoryImpl(createLmuWindowsRainPreferencesDataStore(directory))
