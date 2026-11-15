package kurou.kodriver.core.acewindowsdata.preferences

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository

/**
 * AceWindowsTyreTemperaturePreferences Repository の永続化実装を生成する。
 */
internal fun createAceWindowsTyreTemperaturePreferencesRepository(
    directory: String,
): AceWindowsTyreTemperaturePreferencesRepository =
    AceWindowsTyreTemperaturePreferencesRepositoryImpl(
        dataStore = createAceWindowsTyreTemperaturePreferencesDataStore(directory),
    )
