package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

/**
 * LmuWindowsRemainingVirtualEnergyPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsRemainingVirtualEnergyPreferencesRepository(
    directory: String,
): LmuWindowsRemainingVirtualEnergyPreferencesRepository =
    LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
        createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(directory),
    )
