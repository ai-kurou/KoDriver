package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImpl
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
