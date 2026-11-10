package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository

class SaveMainWindowBoundsUseCase(
    private val repository: MainWindowBoundsPreferencesRepository,
) {
    suspend operator fun invoke(bounds: MainWindowBounds) {
        repository.saveMainWindowBounds(bounds)
    }
}
