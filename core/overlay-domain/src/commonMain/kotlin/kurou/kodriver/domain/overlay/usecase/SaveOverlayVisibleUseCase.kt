package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.repository.OverlayVisiblePreferencesRepository

public class SaveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    public suspend operator fun invoke(visible: Boolean) {
        repository.saveOverlayVisible(visible)
    }
}
