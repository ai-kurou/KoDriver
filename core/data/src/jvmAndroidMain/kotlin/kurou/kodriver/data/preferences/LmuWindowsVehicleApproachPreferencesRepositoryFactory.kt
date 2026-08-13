package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

/**
 * LmuWindowsVehicleApproachPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleApproachPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachPreferencesRepository =
    LmuWindowsVehicleApproachPreferencesRepositoryImpl(
        createLmuWindowsVehicleApproachPreferencesDataStore(directory),
    )
