package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

class ObserveThemeModeUseCase(
    private val repository: ThemePreferencesRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
