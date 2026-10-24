package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

/** ManualReadoutPlayButtonLayoutPreferences Repository の永続化実装を生成する。 */
fun createManualReadoutPlayButtonLayoutPreferencesRepository(
    directory: String,
): ManualReadoutPlayButtonLayoutEnabledPreferencesRepository =
    ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(
        createManualReadoutPlayButtonLayoutPreferencesDataStore(directory),
    )
