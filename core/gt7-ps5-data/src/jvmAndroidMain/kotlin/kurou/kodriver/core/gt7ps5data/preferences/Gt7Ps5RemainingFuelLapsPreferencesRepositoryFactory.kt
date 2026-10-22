package kurou.kodriver.core.gt7ps5data.preferences

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

/**
 * Gt7Ps5RemainingFuelLapsPreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5RemainingFuelLapsPreferencesRepository(
    directory: String,
): Gt7Ps5RemainingFuelLapsPreferencesRepository =
    Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
        dataStore = createGt7Ps5RemainingFuelLapsPreferencesDataStore(directory),
    )
