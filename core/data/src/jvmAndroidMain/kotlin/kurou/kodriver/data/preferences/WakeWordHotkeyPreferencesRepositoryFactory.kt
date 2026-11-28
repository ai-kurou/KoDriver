package kurou.kodriver.data.preferences

import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository

/**
 * WakeWordHotkeyPreferences Repository の永続化実装を生成する。
 */
internal fun createWakeWordHotkeyPreferencesRepository(directory: String): WakeWordHotkeyPreferencesRepository =
    WakeWordHotkeyPreferencesRepositoryImpl(createWakeWordHotkeyPreferencesDataStore(directory))
