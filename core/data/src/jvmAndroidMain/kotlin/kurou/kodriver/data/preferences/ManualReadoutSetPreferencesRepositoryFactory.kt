package kurou.kodriver.data.preferences

import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

/**
 * ManualReadoutSetPreferences Repository の永続化実装を、指定したシミュレーター専用に生成する。
 */
fun createManualReadoutSetPreferencesRepository(
    directory: String,
    simulator: Simulator,
): ManualReadoutSetPreferencesRepository =
    ManualReadoutSetPreferencesRepositoryImpl(
        createManualReadoutSetPreferencesDataStore(directory, simulator),
        // 項目定義の登録は起動時に行われるため、観測の都度、登録済みの移行処理を引く。
        migrateLegacySet = { ReadoutItemKeyRegistry.manualReadoutSpecOf(simulator.id)?.migrateLegacySet ?: { it } },
    )
