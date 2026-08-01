package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleApproachPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleApproachPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

/**
 * LmuWindowsVehicleApproachPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleApproachPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachPreferencesRepository =
    LmuWindowsVehicleApproachPreferencesRepositoryImpl(
        createLmuWindowsVehicleApproachPreferencesDataStore(directory),
    )
