package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsRemainingVirtualEnergyPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

fun createLmuWindowsRemainingVirtualEnergyPreferencesRepository(
    directory: String,
): LmuWindowsRemainingVirtualEnergyPreferencesRepository =
    LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
        createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(directory),
    )
