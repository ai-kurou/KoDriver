package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

/**
 * LmuWindowsVehicleDamagePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleDamagePreferencesRepository(
    directory: String,
): LmuWindowsVehicleDamagePreferencesRepository =
    LmuWindowsVehicleDamagePreferencesRepositoryImpl(
        createLmuWindowsVehicleDamagePreferencesDataStore(directory),
    )
