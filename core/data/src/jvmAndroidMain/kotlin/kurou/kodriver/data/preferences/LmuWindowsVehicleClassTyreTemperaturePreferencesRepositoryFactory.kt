package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

/**
 * LmuWindowsVehicleClassTyreTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(
    directory: String,
): LmuWindowsVehicleClassTyreTemperaturePreferencesRepository =
    LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImpl(
        createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(directory),
    )
