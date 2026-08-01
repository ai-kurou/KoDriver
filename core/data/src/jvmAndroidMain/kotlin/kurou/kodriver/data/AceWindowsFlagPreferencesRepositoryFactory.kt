package kurou.kodriver.data

import kurou.kodriver.data.datasource.createAceWindowsFlagPreferencesDataStore
import kurou.kodriver.data.repository.AceWindowsFlagPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository

/**
 * AceWindowsFlagPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsFlagPreferencesRepository(directory: String): AceWindowsFlagPreferencesRepository =
    AceWindowsFlagPreferencesRepositoryImpl(createAceWindowsFlagPreferencesDataStore(directory))
