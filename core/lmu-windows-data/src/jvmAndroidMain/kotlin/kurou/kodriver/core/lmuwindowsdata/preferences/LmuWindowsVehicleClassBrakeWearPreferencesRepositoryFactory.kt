package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository

/**
 * LmuWindowsVehicleClassBrakeWearPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleClassBrakeWearPreferencesRepository(
    directory: String,
): LmuWindowsVehicleClassBrakeWearPreferencesRepository =
    LmuWindowsVehicleClassBrakeWearPreferencesRepositoryImpl(
        createLmuWindowsVehicleClassBrakeWearPreferencesDataStore(directory),
    )
