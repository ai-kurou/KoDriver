package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository

/**
 * LmuWindowsVehicleClassBrakeTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository(
    directory: String,
): LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository =
    LmuWindowsVehicleClassBrakeTemperaturePreferencesRepositoryImpl(
        createLmuWindowsVehicleClassBrakeTemperaturePreferencesDataStore(directory),
    )
