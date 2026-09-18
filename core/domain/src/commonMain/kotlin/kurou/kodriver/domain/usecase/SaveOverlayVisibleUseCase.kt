package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

class SaveOverlayVisibleUseCase(
    private val repository: OverlayVisiblePreferencesRepository,
) {
    suspend operator fun invoke(visible: Boolean) {
        repository.saveOverlayVisible(visible)
    }
}
