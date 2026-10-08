package kurou.kodriver.data.preferences

/**
 * タイヤ温度の設定・文言 Repository の永続化実装を生成する。
 * 同一ファイルに DataStore を複数作れないため、1つの DataStore を両 Repository で共有する。
 */
fun createLmuWindowsTyreTemperaturePreferencesRepository(
    directory: String,
): LmuWindowsTyreTemperaturePreferencesRepositories {
    val dataStore = createLmuWindowsTyreTemperaturePreferencesDataStore(directory)
    return LmuWindowsTyreTemperaturePreferencesRepositories(
        preferences = LmuWindowsTyreTemperaturePreferencesRepositoryImpl(dataStore),
        readoutText = LmuWindowsTyreTemperatureReadoutTextPreferencesRepositoryImpl(dataStore),
    )
}
