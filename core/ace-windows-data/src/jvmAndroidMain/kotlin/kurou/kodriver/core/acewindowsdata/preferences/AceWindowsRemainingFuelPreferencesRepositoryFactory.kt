package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository

/**
 * AceWindowsRemainingFuelPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsRemainingFuelPreferencesRepository(
    directory: String,
): AceWindowsRemainingFuelPreferencesRepository =
    AceWindowsRemainingFuelPreferencesRepositoryImpl(
        createAceWindowsRemainingFuelPreferencesDataStore(directory),
    )
