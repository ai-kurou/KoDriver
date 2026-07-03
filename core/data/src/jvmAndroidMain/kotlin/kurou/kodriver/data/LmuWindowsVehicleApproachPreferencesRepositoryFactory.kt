package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleApproachPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleApproachPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

fun createLmuWindowsVehicleApproachPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachPreferencesRepository =
    LmuWindowsVehicleApproachPreferencesRepositoryImpl(
        createLmuWindowsVehicleApproachPreferencesDataStore(directory),
    )
