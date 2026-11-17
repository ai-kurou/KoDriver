package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository

public class SaveMainWindowBoundsUseCase(
    private val repository: MainWindowBoundsPreferencesRepository,
) {
    public suspend operator fun invoke(bounds: MainWindowBounds) {
        repository.saveMainWindowBounds(bounds)
    }
}
