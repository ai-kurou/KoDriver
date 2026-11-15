package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository

/**
 * AceWindowsVehicleApproachPreferences Repository の永続化実装を生成する。
 */
internal fun createAceWindowsVehicleApproachPreferencesRepository(
    directory: String,
): AceWindowsVehicleApproachPreferencesRepository =
    AceWindowsVehicleApproachPreferencesRepositoryImpl(
        dataStore = createAceWindowsVehicleApproachPreferencesDataStore(directory),
    )
