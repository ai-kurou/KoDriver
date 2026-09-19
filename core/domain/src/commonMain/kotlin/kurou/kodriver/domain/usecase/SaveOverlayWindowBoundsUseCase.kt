package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository

class SaveOverlayWindowBoundsUseCase(
    private val repository: OverlayWindowBoundsPreferencesRepository,
) {
    suspend operator fun invoke(bounds: OverlayWindowBounds) {
        repository.saveOverlayWindowBounds(bounds)
    }
}
