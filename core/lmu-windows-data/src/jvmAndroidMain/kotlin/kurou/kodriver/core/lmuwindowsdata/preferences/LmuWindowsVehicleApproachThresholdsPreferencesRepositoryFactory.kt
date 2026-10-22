package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

/**
 * LmuWindowsVehicleApproachThresholdsPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository =
    LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl(
        createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(directory),
    )
