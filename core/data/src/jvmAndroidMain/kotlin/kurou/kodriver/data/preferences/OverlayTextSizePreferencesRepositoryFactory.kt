package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

/**
 * OverlayTextSizePreferences Repository の永続化実装を生成する。
 */
fun createOverlayTextSizePreferencesRepository(directory: String): OverlayTextSizePreferencesRepository =
    OverlayTextSizePreferencesRepositoryImpl(createOverlayTextSizePreferencesDataStore(directory))
