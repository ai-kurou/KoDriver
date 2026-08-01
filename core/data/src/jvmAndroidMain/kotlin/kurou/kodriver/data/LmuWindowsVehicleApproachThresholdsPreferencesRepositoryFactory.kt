package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsVehicleApproachThresholdsPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

/**
 * LmuWindowsVehicleApproachThresholdsPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsVehicleApproachThresholdsPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachThresholdsPreferencesRepository =
    LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl(
        createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(directory),
    )
