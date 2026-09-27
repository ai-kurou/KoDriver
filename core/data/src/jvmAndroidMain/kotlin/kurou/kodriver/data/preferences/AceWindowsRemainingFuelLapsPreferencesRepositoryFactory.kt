package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository

/**
 * AceWindowsRemainingFuelLapsPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsRemainingFuelLapsPreferencesRepository(
    directory: String,
): AceWindowsRemainingFuelLapsPreferencesRepository =
    AceWindowsRemainingFuelLapsPreferencesRepositoryImpl(
        dataStore = createAceWindowsRemainingFuelLapsPreferencesDataStore(directory),
    )
