package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

public class SaveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    public suspend operator fun invoke(visible: Boolean) {
        repository.saveOverlayVisible(visible)
    }
}
