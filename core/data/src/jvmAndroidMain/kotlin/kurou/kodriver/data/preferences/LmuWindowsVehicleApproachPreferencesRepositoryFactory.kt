package kurou.kodriver.data.preferences

/**
 * 車両接近の設定・文言 Repository の永続化実装を生成する。
 * 同一ファイルに DataStore を複数作れないため、1つの DataStore を両 Repository で共有する。
 */
fun createLmuWindowsVehicleApproachPreferencesRepository(
    directory: String,
): LmuWindowsVehicleApproachPreferencesRepositories {
    val dataStore = createLmuWindowsVehicleApproachPreferencesDataStore(directory)
    return LmuWindowsVehicleApproachPreferencesRepositories(
        preferences = LmuWindowsVehicleApproachPreferencesRepositoryImpl(dataStore),
        readoutText = LmuWindowsVehicleApproachReadoutTextPreferencesRepositoryImpl(dataStore),
    )
}
