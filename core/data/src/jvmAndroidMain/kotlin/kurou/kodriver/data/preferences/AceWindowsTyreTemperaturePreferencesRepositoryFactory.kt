package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

/**
 * AceWindowsTyreTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsTyreTemperaturePreferencesRepository(
    directory: String,
): AceWindowsTyreTemperaturePreferencesRepository =
    AceWindowsTyreTemperaturePreferencesRepositoryImpl(
        dataStore = createAceWindowsTyreTemperaturePreferencesDataStore(directory),
    )
