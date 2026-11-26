package kurou.kodriver.data.preferences

import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository

/**
 * OverlayBackgroundOpacityPreferences Repository の永続化実装を生成する。
 */
internal fun createOverlayBackgroundOpacityPreferencesRepository(
    directory: String,
): OverlayBackgroundOpacityPreferencesRepository =
    OverlayBackgroundOpacityPreferencesRepositoryImpl(createOverlayBackgroundOpacityPreferencesDataStore(directory))
