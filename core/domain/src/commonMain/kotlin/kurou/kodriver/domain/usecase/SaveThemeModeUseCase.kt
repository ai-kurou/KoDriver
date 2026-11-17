package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

public class SaveThemeModeUseCase(
    private val repository: ThemePreferencesRepository,
) {
    public suspend operator fun invoke(themeMode: ThemeMode): Unit = repository.saveThemeMode(themeMode)
}
