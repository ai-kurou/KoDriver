package kurou.kodriver.data.preferences

/** LMU の現在値読み上げホットキーの永続化実装を生成する。 */
internal fun createReadoutHotkeyPreferencesRepository(directory: String): ReadoutHotkeyPreferencesRepositoryImpl =
    ReadoutHotkeyPreferencesRepositoryImpl(createReadoutHotkeyPreferencesDataStore(directory))
