package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsMyBestLapPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsMyBestLapPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

/**
 * LmuWindowsMyBestLapPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsMyBestLapPreferencesRepository(directory: String): LmuWindowsMyBestLapPreferencesRepository =
    LmuWindowsMyBestLapPreferencesRepositoryImpl(createLmuWindowsMyBestLapPreferencesDataStore(directory))
