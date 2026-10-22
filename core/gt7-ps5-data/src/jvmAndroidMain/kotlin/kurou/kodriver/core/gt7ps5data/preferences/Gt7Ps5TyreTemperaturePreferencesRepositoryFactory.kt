package kurou.kodriver.core.gt7ps5data.preferences

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository

/**
 * Gt7Ps5TyreTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5TyreTemperaturePreferencesRepository(directory: String): Gt7Ps5TyreTemperaturePreferencesRepository =
    Gt7Ps5TyreTemperaturePreferencesRepositoryImpl(
        dataStore = createGt7Ps5TyreTemperaturePreferencesDataStore(directory),
    )
