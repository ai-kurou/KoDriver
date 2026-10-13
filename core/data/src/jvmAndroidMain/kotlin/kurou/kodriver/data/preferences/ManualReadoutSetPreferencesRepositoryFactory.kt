package kurou.kodriver.data.preferences

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

/**
 * ManualReadoutSetPreferences Repository の永続化実装を、指定したシミュレーター専用に生成する。
 */
fun createManualReadoutSetPreferencesRepository(
    directory: String,
    simulator: Simulator,
): ManualReadoutSetPreferencesRepository =
    ManualReadoutSetPreferencesRepositoryImpl(createManualReadoutSetPreferencesDataStore(directory, simulator))
