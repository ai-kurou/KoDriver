package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

/**
 * OverlayTextColorPreferences Repository の永続化実装を生成する。
 */
fun createOverlayTextColorPreferencesRepository(directory: String): OverlayTextColorPreferencesRepository =
    OverlayTextColorPreferencesRepositoryImpl(createOverlayTextColorPreferencesDataStore(directory))
