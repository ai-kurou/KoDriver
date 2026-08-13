package kurou.kodriver.data.preferences

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
