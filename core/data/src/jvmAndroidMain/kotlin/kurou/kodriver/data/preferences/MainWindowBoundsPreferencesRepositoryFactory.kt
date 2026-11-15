package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository

/**
 * MainWindowBoundsPreferences Repository の永続化実装を生成する。
 */
internal fun createMainWindowBoundsPreferencesRepository(directory: String): MainWindowBoundsPreferencesRepository =
    MainWindowBoundsPreferencesRepositoryImpl(createMainWindowBoundsPreferencesDataStore(directory))
