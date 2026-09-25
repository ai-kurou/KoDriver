package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperaturePreferencesRepository

/**
 * LmuWindowsBrakeTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsBrakeTemperaturePreferencesRepository(
    directory: String,
): LmuWindowsBrakeTemperaturePreferencesRepository =
    LmuWindowsBrakeTemperaturePreferencesRepositoryImpl(
        createLmuWindowsBrakeTemperaturePreferencesDataStore(directory),
    )
