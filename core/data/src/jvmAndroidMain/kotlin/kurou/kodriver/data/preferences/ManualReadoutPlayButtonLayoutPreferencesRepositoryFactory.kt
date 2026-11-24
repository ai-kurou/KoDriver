package kurou.kodriver.data.preferences

/** ManualReadoutPlayButtonLayoutPreferences Repository の永続化実装を生成する。 */
internal fun createManualReadoutPlayButtonLayoutPreferencesRepository(
    directory: String,
): ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl =
    ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(
        createManualReadoutPlayButtonLayoutPreferencesDataStore(directory),
    )
