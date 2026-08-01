package kurou.kodriver.data

import kurou.kodriver.data.datasource.createAceWindowsRemainingFuelPreferencesDataStore
import kurou.kodriver.data.repository.AceWindowsRemainingFuelPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

/**
 * AceWindowsRemainingFuelPreferences Repository の永続化実装を生成する。
 */
fun createAceWindowsRemainingFuelPreferencesRepository(directory: String): AceWindowsRemainingFuelPreferencesRepository =
    AceWindowsRemainingFuelPreferencesRepositoryImpl(
        createAceWindowsRemainingFuelPreferencesDataStore(directory),
    )
