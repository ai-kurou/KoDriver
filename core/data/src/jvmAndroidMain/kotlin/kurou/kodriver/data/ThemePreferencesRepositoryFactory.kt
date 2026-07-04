package kurou.kodriver.data

import kurou.kodriver.data.datasource.createThemePreferencesDataStore
import kurou.kodriver.data.repository.ThemePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.ThemePreferencesRepository

fun createThemePreferencesRepository(directory: String): ThemePreferencesRepository =
    ThemePreferencesRepositoryImpl(createThemePreferencesDataStore(directory))
