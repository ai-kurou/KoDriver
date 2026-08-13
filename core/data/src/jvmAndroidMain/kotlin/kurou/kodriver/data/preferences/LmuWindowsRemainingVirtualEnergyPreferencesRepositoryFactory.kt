package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

/**
 * LmuWindowsRemainingVirtualEnergyPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsRemainingVirtualEnergyPreferencesRepository(
    directory: String,
): LmuWindowsRemainingVirtualEnergyPreferencesRepository =
    LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
        createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(directory),
    )
