package kurou.kodriver.data.preferences

/**
 * ピットタイミングの設定・文言 Repository の永続化実装を生成する。
 * 同一ファイルに DataStore を複数作れないため、1つの DataStore を両 Repository で共有する。
 */
fun createLmuWindowsPitTimingPreferencesRepository(directory: String): LmuWindowsPitTimingPreferencesRepositories {
    val dataStore = createLmuWindowsPitTimingPreferencesDataStore(directory)
    return LmuWindowsPitTimingPreferencesRepositories(
        preferences = LmuWindowsPitTimingPreferencesRepositoryImpl(dataStore),
        readoutText = LmuWindowsPitTimingReadoutTextPreferencesRepositoryImpl(dataStore),
    )
}
