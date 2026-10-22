package kurou.kodriver.core.gt7ps5data.preferences

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository

/**
 * Gt7Ps5RemainingFuelPreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5RemainingFuelPreferencesRepository(directory: String): Gt7Ps5RemainingFuelPreferencesRepository =
    Gt7Ps5RemainingFuelPreferencesRepositoryImpl(
        dataStore = createGt7Ps5RemainingFuelPreferencesDataStore(directory),
    )
