package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

/**
 * LmuWindowsMyBestLapPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsMyBestLapPreferencesRepository(directory: String): LmuWindowsMyBestLapPreferencesRepository =
    LmuWindowsMyBestLapPreferencesRepositoryImpl(createLmuWindowsMyBestLapPreferencesDataStore(directory))
