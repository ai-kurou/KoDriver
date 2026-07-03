package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleDamagePreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleDamagePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

fun createLmuWindowsVehicleDamagePreferencesRepository(
    directory: String,
): LmuWindowsVehicleDamagePreferencesRepository =
    LmuWindowsVehicleDamagePreferencesRepositoryImpl(
        createLmuWindowsVehicleDamagePreferencesDataStore(directory),
    )
