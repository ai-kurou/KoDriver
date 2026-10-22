package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

/**
 * AceWindowsRemainingFuelLapsPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsRemainingFuelLapsPreferencesRepository(
    directory: String,
): AceWindowsRemainingFuelLapsPreferencesRepository =
    AceWindowsRemainingFuelLapsPreferencesRepositoryImpl(
        dataStore = createAceWindowsRemainingFuelLapsPreferencesDataStore(directory),
    )
