package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

internal fun createLmuWindowsRemainingFuelPreferencesRepository(
    directory: String,
): LmuWindowsRemainingFuelPreferencesRepository =
    LmuWindowsRemainingFuelPreferencesRepositoryImpl(createLmuWindowsRemainingFuelPreferencesDataStore(directory))
