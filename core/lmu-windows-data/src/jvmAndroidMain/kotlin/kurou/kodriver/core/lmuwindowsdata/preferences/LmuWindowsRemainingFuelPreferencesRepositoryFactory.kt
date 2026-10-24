package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

fun createLmuWindowsRemainingFuelPreferencesRepository(
    directory: String,
): LmuWindowsRemainingFuelPreferencesRepository =
    LmuWindowsRemainingFuelPreferencesRepositoryImpl(createLmuWindowsRemainingFuelPreferencesDataStore(directory))
