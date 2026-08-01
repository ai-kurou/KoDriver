package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleDamagePreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleDamagePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

/**
 * LmuWindowsVehicleDamagePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleDamagePreferencesRepository(directory: String): LmuWindowsVehicleDamagePreferencesRepository =
    LmuWindowsVehicleDamagePreferencesRepositoryImpl(
        createLmuWindowsVehicleDamagePreferencesDataStore(directory),
    )
