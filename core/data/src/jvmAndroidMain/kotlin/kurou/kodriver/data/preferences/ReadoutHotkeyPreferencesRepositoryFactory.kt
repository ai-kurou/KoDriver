package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

/** LMU の現在値読み上げホットキーの永続化実装を生成する。 */
internal fun createReadoutHotkeyPreferencesRepository(directory: String): ReadoutHotkeyPreferencesRepository =
    ReadoutHotkeyPreferencesRepositoryImpl(createReadoutHotkeyPreferencesDataStore(directory))
