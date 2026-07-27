package kurou.kodriver.data

import kurou.kodriver.data.datasource.createAceWindowsRemainingFuelPreferencesDataStore
import kurou.kodriver.data.repository.AceWindowsRemainingFuelPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

fun createAceWindowsRemainingFuelPreferencesRepository(
    directory: String,
): AceWindowsRemainingFuelPreferencesRepository =
    AceWindowsRemainingFuelPreferencesRepositoryImpl(
        createAceWindowsRemainingFuelPreferencesDataStore(directory),
    )
