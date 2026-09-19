package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

/**
 * OverlayVisiblePreferences Repository の永続化実装を生成する。
 */
fun createOverlayVisiblePreferencesRepository(directory: String): OverlayVisiblePreferencesRepository =
    OverlayVisiblePreferencesRepositoryImpl(createOverlayVisiblePreferencesDataStore(directory))
