package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository

/**
 * LmuWindowsVehicleDamagePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleDamagePreferencesRepository(
    directory: String,
): LmuWindowsVehicleDamagePreferencesRepository =
    LmuWindowsVehicleDamagePreferencesRepositoryImpl(
        createLmuWindowsVehicleDamagePreferencesDataStore(directory),
    )
