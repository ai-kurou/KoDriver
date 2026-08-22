package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

/**
 * AceWindowsVehicleApproachPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsVehicleApproachPreferencesRepository(
    directory: String,
): AceWindowsVehicleApproachPreferencesRepository =
    AceWindowsVehicleApproachPreferencesRepositoryImpl(
        dataStore = createAceWindowsVehicleApproachPreferencesDataStore(directory),
    )
