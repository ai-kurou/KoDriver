package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository

/**
 * AceWindowsFlagPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsFlagPreferencesRepository(directory: String): AceWindowsFlagPreferencesRepository =
    AceWindowsFlagPreferencesRepositoryImpl(createAceWindowsFlagPreferencesDataStore(directory))
