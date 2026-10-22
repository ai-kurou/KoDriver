package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository

/**
 * AceWindowsMyBestLapPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsMyBestLapPreferencesRepository(directory: String): AceWindowsMyBestLapPreferencesRepository =
    AceWindowsMyBestLapPreferencesRepositoryImpl(createAceWindowsMyBestLapPreferencesDataStore(directory))
