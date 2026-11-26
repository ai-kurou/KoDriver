package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository

/**
 * AceWindowsBrakeTemperaturePreferences Repository の永続化実装を生成する。
 */
internal fun createAceWindowsBrakeTemperaturePreferencesRepository(
    directory: String,
): AceWindowsBrakeTemperaturePreferencesRepository =
    AceWindowsBrakeTemperaturePreferencesRepositoryImpl(
        dataStore = createAceWindowsBrakeTemperaturePreferencesDataStore(directory),
    )
