package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository

/**
 * AceWindowsFlagReadoutTextPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsFlagReadoutTextPreferencesRepository(
    directory: String,
): AceWindowsFlagReadoutTextPreferencesRepository =
    AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
        createAceWindowsFlagReadoutTextPreferencesDataStore(directory),
    )
