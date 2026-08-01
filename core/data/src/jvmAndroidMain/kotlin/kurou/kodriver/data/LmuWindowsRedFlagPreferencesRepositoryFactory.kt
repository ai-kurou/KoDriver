package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsRedFlagPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsRedFlagPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository

/**
 * LmuWindowsRedFlagPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsRedFlagPreferencesRepository(directory: String): LmuWindowsRedFlagPreferencesRepository =
    LmuWindowsRedFlagPreferencesRepositoryImpl(createLmuWindowsRedFlagPreferencesDataStore(directory))
