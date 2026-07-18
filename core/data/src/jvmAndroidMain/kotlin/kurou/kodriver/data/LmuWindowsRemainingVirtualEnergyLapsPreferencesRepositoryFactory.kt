package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsRemainingVirtualEnergyLapsPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

fun createLmuWindowsRemainingVirtualEnergyLapsPreferencesRepository(
    directory: String,
): LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository =
    LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryImpl(
        dataStore = createLmuWindowsRemainingVirtualEnergyLapsPreferencesDataStore(directory),
    )
