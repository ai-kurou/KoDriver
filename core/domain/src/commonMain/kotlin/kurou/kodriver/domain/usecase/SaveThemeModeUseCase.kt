package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

class SaveThemeModeUseCase(private val repository: ThemePreferencesRepository) {
    suspend operator fun invoke(themeMode: ThemeMode) = repository.saveThemeMode(themeMode)
}
