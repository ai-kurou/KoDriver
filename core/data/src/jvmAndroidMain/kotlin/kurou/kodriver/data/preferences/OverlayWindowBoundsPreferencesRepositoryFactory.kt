package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository

/**
 * OverlayWindowBoundsPreferences Repository の永続化実装を生成する。
 */
fun createOverlayWindowBoundsPreferencesRepository(directory: String): OverlayWindowBoundsPreferencesRepository =
    OverlayWindowBoundsPreferencesRepositoryImpl(createOverlayWindowBoundsPreferencesDataStore(directory))
