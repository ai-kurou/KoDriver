package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository

/**
 * AceWindowsMyBestLapPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsMyBestLapPreferencesRepository(directory: String): AceWindowsMyBestLapPreferencesRepository =
    AceWindowsMyBestLapPreferencesRepositoryImpl(createAceWindowsMyBestLapPreferencesDataStore(directory))
