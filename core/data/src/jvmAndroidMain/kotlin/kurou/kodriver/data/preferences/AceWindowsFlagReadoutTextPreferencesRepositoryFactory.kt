package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

/**
 * AceWindowsFlagReadoutTextPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsFlagReadoutTextPreferencesRepository(
    directory: String,
): AceWindowsFlagReadoutTextPreferencesRepository =
    AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
        createAceWindowsFlagReadoutTextPreferencesDataStore(directory),
    )
