package kurou.kodriver.data

import kurou.kodriver.data.datasource.createGt7Ps5TyreTemperaturePreferencesDataStore
import kurou.kodriver.data.repository.Gt7Ps5TyreTemperaturePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

/**
 * Gt7Ps5TyreTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5TyreTemperaturePreferencesRepository(directory: String): Gt7Ps5TyreTemperaturePreferencesRepository =
    Gt7Ps5TyreTemperaturePreferencesRepositoryImpl(
        dataStore = createGt7Ps5TyreTemperaturePreferencesDataStore(directory),
    )
