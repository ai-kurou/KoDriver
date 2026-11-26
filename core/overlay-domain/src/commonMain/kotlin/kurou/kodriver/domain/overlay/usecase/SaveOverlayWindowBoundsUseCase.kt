package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.model.OverlayWindowBounds
import kurou.kodriver.domain.overlay.repository.OverlayWindowBoundsPreferencesRepository

public class SaveOverlayWindowBoundsUseCase(
    private val repository: OverlayWindowBoundsPreferencesRepository,
) {
    public suspend operator fun invoke(bounds: OverlayWindowBounds) {
        repository.saveOverlayWindowBounds(bounds)
    }
}
