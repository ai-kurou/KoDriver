package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

/**
 * OverlayBackgroundOpacityPreferences Repository の永続化実装を生成する。
 */
fun createOverlayBackgroundOpacityPreferencesRepository(
    directory: String,
): OverlayBackgroundOpacityPreferencesRepository =
    OverlayBackgroundOpacityPreferencesRepositoryImpl(createOverlayBackgroundOpacityPreferencesDataStore(directory))
