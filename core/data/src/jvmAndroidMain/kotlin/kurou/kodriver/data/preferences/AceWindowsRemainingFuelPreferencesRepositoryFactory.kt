package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

/**
 * AceWindowsRemainingFuelPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsRemainingFuelPreferencesRepository(
    directory: String,
): AceWindowsRemainingFuelPreferencesRepository =
    AceWindowsRemainingFuelPreferencesRepositoryImpl(
        createAceWindowsRemainingFuelPreferencesDataStore(directory),
    )
