package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository

/**
 * LmuWindowsOverheatPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsOverheatPreferencesRepository(directory: String): LmuWindowsOverheatPreferencesRepository =
    LmuWindowsOverheatPreferencesRepositoryImpl(createLmuWindowsOverheatPreferencesDataStore(directory))
