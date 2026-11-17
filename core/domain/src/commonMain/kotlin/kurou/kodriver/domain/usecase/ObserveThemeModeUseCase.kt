package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

public class ObserveThemeModeUseCase(
    private val repository: ThemePreferencesRepository,
) {
    public operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
