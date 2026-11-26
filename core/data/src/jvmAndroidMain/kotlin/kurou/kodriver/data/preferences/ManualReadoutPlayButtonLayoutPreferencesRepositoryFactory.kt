package kurou.kodriver.data.preferences

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

/** ManualReadoutPlayButtonLayoutPreferences Repository の永続化実装を生成する。 */
internal fun createManualReadoutPlayButtonLayoutPreferencesRepository(
    directory: String,
): ManualReadoutPlayButtonLayoutEnabledPreferencesRepository =
    ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(
        createManualReadoutPlayButtonLayoutPreferencesDataStore(directory),
    )
